FROM node:latest

# ENV http_proxy http://<proxy_host>:<proxy_port>
# ENV https_proxy http://<proxy_host>:<proxy_port>

# Create app directory
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# Install app dependencies
COPY package.json /usr/src/app/
RUN npm install

# Bundle app source
COPY . /usr/src/app

RUN npm run clean && npm run build


EXPOSE 3000
CMD [ "npm","run", "start" ]

