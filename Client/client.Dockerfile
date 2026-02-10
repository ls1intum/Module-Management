FROM node:24-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install --legacy-peer-deps

COPY . .
RUN npm run build

FROM nginx:1.29.4

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=builder /app/dist/module-management-client/browser /usr/share/nginx/html