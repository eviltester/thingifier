FROM python:3
RUN wget https://github.com/AutomationPanda/device-registry-fastapi/archive/refs/heads/main.zip
RUN mkdir /home/device-registry-fastapi
RUN unzip main.zip -d /home/device-registry-fastapi
RUN rm main.zip
RUN cp -r /home/device-registry-fastapi/device-registry-fastapi-main /usr/src/app

WORKDIR /usr/src/app
RUN pip install -r requirements.txt

EXPOSE 8000

# configure to use the dev database with data
RUN sed -i 's/"database": "test"/"database": "dev"/g' config.json

# need to override the 127.0.0.1 default to allow external access
CMD [ "uvicorn", "--host", "0.0.0.0", "app.main:app" ]