FROM node:18-alpine
RUN wget https://github.com/sd576/FX-TradeHub-API/archive/refs/heads/main.zip
RUN mkdir /home/FX-TradeHub-API
RUN unzip main.zip -d /home/FX-TradeHub-API
RUN cd /home/FX-TradeHub-API/FX-TradeHub-API-main

WORKDIR /home/FX-TradeHub-API/FX-TradeHub-API-main
RUN npm install

EXPOSE 3000

CMD [ "npm", "run", "setup-and-start" ]