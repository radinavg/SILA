# Stage 1: Build the Angular application
FROM node:18.19.0 AS build

WORKDIR /app

COPY package*.json ./

RUN npm install

# Install Angular CLI globally
RUN npm install -g @angular/cli

# Copy all files and build the application
COPY . .
RUN ng build --output-path=dist/frontend --configuration=production

# Stage 2: Serve with NGINX
FROM nginx:latest

# COPY nginx.conf /etc/nginx/nginx.conf

# Copy the build output to the NGINX HTML directory
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Expose port 80
EXPOSE 80
