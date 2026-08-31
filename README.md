# openfs

## S3-compatible file server

| Command | Request | Status |
|---|---|---|
| CreateBucket | `PUT /{bucket}` | ✅ |
| ListBuckets | `GET /` | ❌ |
| DeleteBucket | `DELETE /{bucket}` | ❌ |
| HeadBucket | `HEAD /{bucket}` | ✅ |
| GetBucketLocation | `GET /{bucket}?location` | ❌ |
