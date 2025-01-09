FROM node:22-alpine AS builder
<<<<<<< HEAD
=======

>>>>>>> 9dde151198509cfbe51c6a0b9d771c3cce10b8f7
WORKDIR /app
COPY package*.json ./
RUN npm install --legacy-peer-deps

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist/module-management-client/browser /usr/share/nginx/html
