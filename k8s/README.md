# Spring-samples - Kubernetes 

## Run with Kubernetes 

- `kubectl apply -f appdb.yaml`
- `kubectl apply -f appbe.yaml`

## Delete Kubernetes objects

- `kubectl delete -f appdb.yaml`
- `kubectl delete -f appbe.yaml`

## Check manifest

- `kubectl apply -f appbe_probes.yaml --dry-run=client`

or

- `kubectl apply -f appbe_probes.yaml --dry-run=server`
