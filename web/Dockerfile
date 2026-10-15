FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY app /usr/share/nginx/html/app
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
