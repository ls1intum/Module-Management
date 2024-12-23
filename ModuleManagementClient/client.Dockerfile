FROM node:22-alpine as builder

WORKDIR /app

COPY package*.json ./
RUN npm install --legacy-peer-deps

COPY . .

RUN npm run build
FROM nginx:alpine
COPY --from=builder /app/dist/module-management-client/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf