from flask import Flask, request, jsonify
import os
import numpy as np
import tensorflow as tf
from pathlib import Path
from tensorflow.keras.preprocessing import image

app = Flask(__name__)

input_shape = (200, 200, 3)
class_subset = sorted([
    "apple_pie", "cannoli", "cheese_plate", "chicken_wings", "chocolate_cake", "club_sandwich", 
    "donuts", "dumplings", "french_fries", "fried_rice", "garlic_bread", "greek_salad", 
    "grilled_salmon", "hamburger", "hot_dog", "ice_cream", "lasagna", "macarons", "nachos", 
    "omelette", "pancakes", "pizza", "ramen", "risotto", "spaghetti_bolognese", "steak", 
    "strawberry_shortcake", "sushi", "tacos", "waffles"
])
n_classes = len(class_subset)
target_size = (200, 200)

initial_learning_rate = 0.002
lr_schedule = tf.keras.optimizers.schedules.ExponentialDecay(
    initial_learning_rate, decay_steps=100000, decay_rate=0.96, staircase=True)
optim_resnet = tf.keras.optimizers.Adam(learning_rate=lr_schedule)


def create_model_resnet(input_shape, n_classes, optimizer, fine_tune=0, weights_path=None):
    try:
        conv_base = tf.keras.applications.ResNet50(include_top=False, weights='imagenet', input_shape=input_shape)
        
        if fine_tune > 0:
            for layer in conv_base.layers[:-fine_tune]:
                layer.trainable = False
        else:
            for layer in conv_base.layers:
                layer.trainable = False

        model = tf.keras.Sequential([
            conv_base,
            tf.keras.layers.GlobalAveragePooling2D(),
            tf.keras.layers.Dense(1024, activation='relu'),
            tf.keras.layers.BatchNormalization(),
            tf.keras.layers.Dropout(0.3),
            tf.keras.layers.Dense(n_classes, activation='softmax')
        ])

        model.compile(optimizer=optimizer, loss='categorical_crossentropy', 
                      metrics=['accuracy', tf.keras.metrics.Precision(name='precision'), tf.keras.metrics.Recall(name='recall')])

        if weights_path:
            if os.path.exists(weights_path):
                model.load_weights(weights_path)
                print(f"Model weights loaded from {weights_path}")
            else:
                raise FileNotFoundError(f"Weight file {weights_path} not found.")

        return model
    
    except Exception as e:
        print(f"Error creating model: {e}")
        raise

# Load the model when the Flask app starts
try:
    resnet_model = create_model_resnet(input_shape, n_classes, optim_resnet, fine_tune=8, weights_path='tl_model_resnet.weights.best.hdf5')
    class_indices_resnet = {i: class_name for i, class_name in enumerate(class_subset)}
except Exception as e:
    print(f"Error loading model: {e}")
    resnet_model = None

# Function to preprocess the image
def preprocess_single_image(img_path, target_size):
    try:
        if not img_path.exists() or not img_path.is_file():
            raise FileNotFoundError(f"Image file {img_path} not found or is not a file.")
        
        img = image.load_img(img_path, target_size=target_size)
        img_array = image.img_to_array(img)
        img_array = np.expand_dims(img_array, axis=0)
        img_array = tf.keras.applications.resnet.preprocess_input(img_array)
        return img_array
    
    except Exception as e:
        print(f"Error preprocessing image: {e}")
        raise

def classify_single_image(model, img_array, class_indices):
    try:
        preds = model.predict(img_array, verbose=0)
        pred_class_index = np.argmax(preds, axis=1)[0]
        pred_class_label = class_indices[pred_class_index]
        return pred_class_label
    
    except Exception as e:
        print(f"Error during image classification: {e}")
        raise


@app.route('/predict', methods=['POST'])
def predict():
    try:
        data = request.json
        if not data or 'file_uri' not in data:
            return jsonify({"error": "No file URI provided"}), 400

        img_path = data['file_uri']        

        safe_path = Path(img_path).resolve()
       
        img_array = preprocess_single_image(safe_path, target_size)
        predicted_class = classify_single_image(resnet_model, img_array, class_indices_resnet)
        return jsonify({"predicted_class": predicted_class})
    
    except FileNotFoundError as e:
        return jsonify({"error": str(e)}), 404
    except ValueError as e:
        return jsonify({"error": str(e)}), 400
    except Exception as e:
        return jsonify({"error": "An unexpected error occurred.", "details": str(e)}), 500

if __name__ == "__main__":

    app.run(host='127.0.0.1', port=5000, debug=False)