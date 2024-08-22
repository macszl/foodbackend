FROM maven:3.9.6-amazoncorretto-17 as build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -X

FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y python3 python3-pip && apt-get clean

COPY requirements.txt /app/requirements.txt
RUN pip3 install --no-cache-dir -r /app/requirements.txt

RUN apt-get update && apt-get install -y nginx supervisor gettext sed && apt-get clean

COPY --from=build /app/target/foodbackend-0.0.1-SNAPSHOT.jar /app/foodbackend.jar

EXPOSE 80

RUN apt-get update && apt-get install -y git-lfs && apt-get clean
RUN git lfs install
RUN git clone https://huggingface.co/macszlen/food-101-resnet/ /app/food-101-resnet

COPY flask_app.py /app/flask_app.py

COPY modify_conf.sh /app/modify_conf.sh
RUN chmod +x /app/modify_conf.sh

COPY nginx.conf /etc/nginx/nginx.conf.template
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
ENTRYPOINT ["supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
