FROM node:14 AS build

WORKDIR /build

COPY package*.json /build/

RUN ["npm","install","--registry=https://registry.npm.taobao.org", "--production"]

COPY  .  /build/

RUN ["npm","run","build"]




FROM node:14-alpine AS release

WORKDIR /run

COPY package*.json /run

RUN ["npm","install","--registry=https://registry.npm.taobao.org", "--production"]

COPY --from=build /build/.nuxt /run/.nuxt

EXPOSE 3000

CMD ["npm", "run", "start"]
