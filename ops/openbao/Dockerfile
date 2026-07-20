FROM alpine:3.22

RUN apk add --no-cache ca-certificates curl jq

COPY ops/openbao/bin/render-secrets.sh /usr/local/bin/render-secrets
RUN chmod 0555 /usr/local/bin/render-secrets

ENTRYPOINT ["/usr/local/bin/render-secrets"]
