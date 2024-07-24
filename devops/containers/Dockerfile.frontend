FROM node:16.20.2-alpine3.18 as builder
WORKDIR /build
RUN mkdir /build/project
COPY src/package.json /build
ARG CI_COMMIT_BRANCH

RUN npm config set registry https://nexus.dew.org/repository/npm/ && \
    npm config set unsafe-perm true && \
    npm install -f

COPY src/ /build/
RUN npm run build && \
    cp -vr public/* dist/

FROM nginx:1.21.6-alpine
RUN rm -rf /usr/share/nginx/html/*
COPY --from=builder /build/dist/ /usr/share/nginx/html/
COPY default.conf /etc/nginx/conf.d/default.conf
