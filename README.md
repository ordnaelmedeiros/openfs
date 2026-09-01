# openfs

## S3-compatible file server

## Bucket

| Command | Request | Status |
|---|---|---|
| CreateBucket | `PUT /{bucket}` | ✅ |
| ListBuckets | `GET /` | ✅ |
| DeleteBucket | `DELETE /{bucket}` | ✅ |
| HeadBucket | `HEAD /{bucket}` | ✅ |
| GetBucketLocation | `GET /{bucket}?location` | ❌ |

## Object

| Command | Request | Status |
|---|---|---|
| PutObject | `PUT /{bucket}/{key}` | ❌ |
| GetObject | `GET /{bucket}/{key}` | ❌ |
| HeadObject | `HEAD /{bucket}/{key}` | ❌ |
| DeleteObject | `DELETE /{bucket}/{key}` | ❌ |
| DeleteObjects | `POST /{bucket}?delete` | ❌ |
| CopyObject | `PUT /{bucket}/{key}` + `x-amz-copy-source` | ❌ |
| ListObjectsV2 | `GET /{bucket}?list-type=2` | ✅ |
| ListObjects | `GET /{bucket}` | ✅ |
| CreateMultipartUpload | `POST /{bucket}/{key}?uploads` | ❌ |
| UploadPart | `PUT /{bucket}/{key}?partNumber={n}&uploadId={id}` | ❌ |
| UploadPartCopy | `PUT /{bucket}/{key}?partNumber={n}&uploadId={id}` + `x-amz-copy-source` | ❌ |
| CompleteMultipartUpload | `POST /{bucket}/{key}?uploadId={id}` | ❌ |
| AbortMultipartUpload | `DELETE /{bucket}/{key}?uploadId={id}` | ❌ |
| ListMultipartUploads | `GET /{bucket}?uploads` | ❌ |
| ListParts | `GET /{bucket}/{key}?uploadId={id}` | ❌ |
