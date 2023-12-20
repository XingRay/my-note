## Harbor Installation and Configuration



This section describes how to perform a new installation of Harbor.

If you are upgrading from a previous version of Harbor, you might need to update the configuration file and migrate your data to fit the database schema of the later version. For information about upgrading, see [Upgrading Harbor](https://goharbor.io/docs/2.8.0/administration/upgrade/).

Before you install Harbor, you can test the latest version of Harbor on a demo environment maintained by the Harbor team. For information, see [Test Harbor with the Demo Server](https://goharbor.io/docs/2.8.0/install-config/demo-server/).

Harbor supports integration with different 3rd-party replication adapters for replicating data, OIDC adapters for authN/authZ, and scanner adapters for vulnerability scanning of container images. For information about the supported adapters, see the [Harbor Compatibility List](https://goharbor.io/docs/2.8.0/install-config/harbor-compatibility-list/).

## Installation Process

The standard Harbor installation process involves the following stages:

1. Make sure that your target host meets the [Harbor Installation Prerequisites](https://goharbor.io/docs/2.8.0/install-config/installation-prereqs/).
2. [Download the Harbor Installer](https://goharbor.io/docs/2.8.0/install-config/download-installer/)
3. [Configure HTTPS Access to Harbor](https://goharbor.io/docs/2.8.0/install-config/configure-https/)
4. [Configure the Harbor YML File](https://goharbor.io/docs/2.8.0/install-config/configure-yml-file/)
5. [Configure Enabling Internal TLS](https://goharbor.io/docs/2.8.0/install-config/configure-internal-tls/)
6. [Run the Installer Script](https://goharbor.io/docs/2.8.0/install-config/run-installer-script/)

If installation fails, see [Troubleshooting Harbor Installation](https://goharbor.io/docs/2.8.0/install-config/troubleshoot-installation/).

## Deploy Harbor on Kubernetes

You can also use Helm to install Harbor on a Kubernetes cluster, to make Harbor highly available. For information about installing Harbor with Helm on a Kubernetes cluster, see [Deploying Harbor with High Availability via Helm](https://goharbor.io/docs/2.8.0/install-config/harbor-ha-helm/).

## Post-Installation Configuration

For information about how to manage your deployed Harbor instance, see [Reconfigure Harbor and Manage the Harbor Lifecycle](https://goharbor.io/docs/2.8.0/install-config/reconfigure-manage-lifecycle/).

By default, Harbor uses its own private key and certificate to authenticate with Docker. For information about how to optionally customize your configuration to use your own key and certificate, see [Customize the Harbor Token Service](https://goharbor.io/docs/2.8.0/install-config/customize-token-service/).

After installation, log into your Harbor via the web console to configure the instance under ‘configuration’. Harbor also provides a command line interface (CLI) that allows you to [Configure Harbor System Settings at the Command Line](https://goharbor.io/docs/2.8.0/install-config/configure-system-settings-cli/).

## Harbor Components

The table below lists the some of the key components that are deployed when you deploy Harbor.

| Component           | Version |
| :------------------ | :------ |
| Postgresql          | 13.3.0  |
| Redis               | 6.0.13  |
| Beego               | 1.9.0   |
| Docker/distribution | 2.7.1   |
| Docker/notary       | 0.6.1   |
| Helm                | 2.9.1   |
| Swagger-ui          | 3.22.1  |

------

## Pages in this section

- [Test Harbor with the Demo Server](https://goharbor.io/docs/2.8.0/install-config/demo-server/)
- [Harbor Compatibility List](https://goharbor.io/docs/2.8.0/install-config/harbor-compatibility-list/)
- [Harbor Installation Prerequisites](https://goharbor.io/docs/2.8.0/install-config/installation-prereqs/)
- [Download the Harbor Installer](https://goharbor.io/docs/2.8.0/install-config/download-installer/)
- [Configure HTTPS Access to Harbor](https://goharbor.io/docs/2.8.0/install-config/configure-https/)
- [Configure Internal TLS communication between Harbor Component](https://goharbor.io/docs/2.8.0/install-config/configure-internal-tls/)
- [Configure the Harbor YML File](https://goharbor.io/docs/2.8.0/install-config/configure-yml-file/)
- [Run the Installer Script](https://goharbor.io/docs/2.8.0/install-config/run-installer-script/)
- [Deploying Harbor with High Availability via Helm](https://goharbor.io/docs/2.8.0/install-config/harbor-ha-helm/)
- [Troubleshooting Harbor Installation](https://goharbor.io/docs/2.8.0/install-config/troubleshoot-installation/)
- [Reconfigure Harbor and Manage the Harbor Lifecycle](https://goharbor.io/docs/2.8.0/install-config/reconfigure-manage-lifecycle/)
- [Customize the Harbor Token Service](https://goharbor.io/docs/2.8.0/install-config/customize-token-service/)
- [Harbor Configuration](https://goharbor.io/docs/2.8.0/install-config/configure-system-settings-cli/)





### Harbor Installation Prerequisites

Harbor is deployed as several Docker containers. You can therefore deploy it on any Linux distribution that supports Docker. The target host requires Docker, and Docker Compose to be installed.

### Hardware

The following table lists the minimum and recommended hardware configurations for deploying Harbor.

| Resource | Minimum | Recommended |
| :------- | :------ | :---------- |
| CPU      | 2 CPU   | 4 CPU       |
| Mem      | 4 GB    | 8 GB        |
| Disk     | 40 GB   | 160 GB      |

### Software

The following table lists the software versions that must be installed on the target host.

| Software       | Version                                                      | Description                                                  |
| :------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| Docker Engine  | Version 17.06.0-ce+ or higher                                | For installation instructions, see [Docker Engine documentation](https://docs.docker.com/engine/installation/) |
| Docker Compose | docker-compose (v1.18.0+) or docker compose v2 (docker-compose-plugin) | For installation instructions, see [Docker Compose documentation](https://docs.docker.com/compose/install/) |
| OpenSSL        | Latest is preferred                                          | Used to generate certificate and keys for Harbor             |

### Network ports

Harbor requires that the following ports be open on the target host.

| Port | Protocol | Description                                                  |
| :--- | :------- | :----------------------------------------------------------- |
| 443  | HTTPS    | Harbor portal and core API accept HTTPS requests on this port. You can change this port in the configuration file. |
| 4443 | HTTPS    | Connections to the Docker Content Trust service for Harbor. Only required if Notary is enabled. You can change this port in the configuration file. |
| 80   | HTTP     | Harbor portal and core API accept HTTP requests on this port. You can change this port in the configuration file. |

## What to Do Next

[Download the Harbor Installer](https://goharbor.io/docs/2.8.0/install-config/download-installer/).





### Download the Harbor Installer

You download the Harbor installers from the [official releases](https://github.com/goharbor/harbor/releases) page. Download either the online installer or the offline installer.

- **Online installer:** The online installer downloads the Harbor images from Docker hub. For this reason, the installer is very small in size.
- **Offline installer:** Use the offline installer if the host to which are deploying Harbor does not have a connection to the Internet. The offline installer contains pre-built images, so it is larger than the online installer.

The installation processes are almost the same for the online and offline installers.

## Download and Unpack the Installer

1. Go to the [Harbor releases page](https://github.com/goharbor/harbor/releases).

2. Download the online or offline installer for the version you want to install.

3. Optionally download the corresponding `*.asc` file to verify that the package is genuine.

   The `*.asc` file is an OpenPGP key file. Perform the following steps to verify that the downloaded bundle is genuine.

   1. Obtain the public key for the `*.asc` file.

      ```sh
      gpg --keyserver hkps://keyserver.ubuntu.com --receive-keys 644FF454C0B4115C
      ```

      You should see the message `public key "Harbor-sign (The key for signing Harbor build) <jiangd@vmware.com>" imported`

   2. Verify that the package is genuine by running one of the following commands.

      - Online installer:

        ```sh
        gpg -v --keyserver hkps://keyserver.ubuntu.com --verify harbor-online-installer-version.tgz.asc
        ```

      - Offline installer:

        ```sh
        gpg -v --keyserver hkps://keyserver.ubuntu.com --verify harbor-offline-installer-version.tgz.asc
        ```

      The `gpg` command verifies that the bundle’s signature matches that of the `*.asc` key file. You should see confirmation that the signature is correct.

      ```sh
      gpg: armor header: Version: GnuPG v1
      gpg: assuming signed data in 'harbor-online-installer-v2.0.2.tgz'
      gpg: Signature made Tue Jul 28 09:49:20 2020 UTC
      gpg:                using RSA key 644FF454C0B4115C
      gpg: using pgp trust model
      gpg: Good signature from "Harbor-sign (The key for signing Harbor build) <jiangd@vmware.com>" [unknown]
      gpg: WARNING: This key is not certified with a trusted signature!
      gpg:          There is no indication that the signature belongs to the owner.
      Primary key fingerprint: 7722 D168 DAEC 4578 06C9  6FF9 644F F454 C0B4 115C
      gpg: binary signature, digest algorithm SHA1, key algorithm rsa4096
      ```

4. Use `tar` to extract the installer package:

   - Online installer:

     ```sh
     bash $ tar xzvf harbor-online-installer-version.tgz
     ```

   - Offline installer:

     ```sh
     bash $ tar xzvf harbor-offline-installer-version.tgz
     ```

## Next Steps

- To secure the connections to Harbor, see [Configure HTTPS Access to Harbor](https://goharbor.io/docs/2.8.0/install-config/configure-https/).
- To configure your Harbor installation, see [Configure the Harbor YML File](https://goharbor.io/docs/2.8.0/install-config/configure-yml-file/).



### Configure HTTPS Access to Harbor

By default, Harbor does not ship with certificates. It is possible to deploy Harbor without security, so that you can connect to it over HTTP. However, using HTTP is acceptable only in air-gapped test or development environments that do not have a connection to the external internet. Using HTTP in environments that are not air-gapped exposes you to man-in-the-middle attacks. In production environments, always use HTTPS. If you enable Content Trust with Notary to properly sign all images, you must use HTTPS.

To configure HTTPS, you must create SSL certificates. You can use certificates that are signed by a trusted third-party CA, or you can use self-signed certificates. This section describes how to use [OpenSSL](https://www.openssl.org/) to create a CA, and how to use your CA to sign a server certificate and a client certificate. You can use other CA providers, for example [Let’s Encrypt](https://letsencrypt.org/).

The procedures below assume that your Harbor registry’s hostname is `yourdomain.com`, and that its DNS record points to the host on which you are running Harbor.

## Generate a Certificate Authority Certificate

In a production environment, you should obtain a certificate from a CA. In a test or development environment, you can generate your own CA. To generate a CA certficate, run the following commands.

1. Generate a CA certificate private key.

   ```sh
   openssl genrsa -out ca.key 4096
   ```

2. Generate the CA certificate.

   Adapt the values in the `-subj` option to reflect your organization. If you use an FQDN to connect your Harbor host, you must specify it as the common name (`CN`) attribute.

   ```sh
   openssl req -x509 -new -nodes -sha512 -days 3650 \
    -subj "/C=CN/ST=Beijing/L=Beijing/O=example/OU=Personal/CN=yourdomain.com" \
    -key ca.key \
    -out ca.crt
   ```

## Generate a Server Certificate

The certificate usually contains a `.crt` file and a `.key` file, for example, `yourdomain.com.crt` and `yourdomain.com.key`.

1. Generate a private key.

   ```sh
   openssl genrsa -out yourdomain.com.key 4096
   ```

2. Generate a certificate signing request (CSR).

   Adapt the values in the `-subj` option to reflect your organization. If you use an FQDN to connect your Harbor host, you must specify it as the common name (`CN`) attribute and use it in the key and CSR filenames.

   ```sh
   openssl req -sha512 -new \
       -subj "/C=CN/ST=Beijing/L=Beijing/O=example/OU=Personal/CN=yourdomain.com" \
       -key yourdomain.com.key \
       -out yourdomain.com.csr
   ```

3. Generate an x509 v3 extension file.

   Regardless of whether you’re using either an FQDN or an IP address to connect to your Harbor host, you must create this file so that you can generate a certificate for your Harbor host that complies with the Subject Alternative Name (SAN) and x509 v3 extension requirements. Replace the `DNS` entries to reflect your domain.

   ```sh
   cat > v3.ext <<-EOF
   authorityKeyIdentifier=keyid,issuer
   basicConstraints=CA:FALSE
   keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
   extendedKeyUsage = serverAuth
   subjectAltName = @alt_names
   
   [alt_names]
   DNS.1=yourdomain.com
   DNS.2=yourdomain
   DNS.3=hostname
   EOF
   ```

4. Use the `v3.ext` file to generate a certificate for your Harbor host.

   Replace the `yourdomain.com` in the CRS and CRT file names with the Harbor host name.

   ```sh
   openssl x509 -req -sha512 -days 3650 \
       -extfile v3.ext \
       -CA ca.crt -CAkey ca.key -CAcreateserial \
       -in yourdomain.com.csr \
       -out yourdomain.com.crt
   ```

## Provide the Certificates to Harbor and Docker

After generating the `ca.crt`, `yourdomain.com.crt`, and `yourdomain.com.key` files, you must provide them to Harbor and to Docker, and reconfigure Harbor to use them.

1. Copy the server certificate and key into the certficates folder on your Harbor host.

   ```sh
   cp yourdomain.com.crt /data/cert/
   cp yourdomain.com.key /data/cert/
   ```

2. Convert `yourdomain.com.crt` to `yourdomain.com.cert`, for use by Docker.

   The Docker daemon interprets `.crt` files as CA certificates and `.cert` files as client certificates.

   ```sh
   openssl x509 -inform PEM -in yourdomain.com.crt -out yourdomain.com.cert
   ```

3. Copy the server certificate, key and CA files into the Docker certificates folder on the Harbor host. You must create the appropriate folders first.

   ```sh
   cp yourdomain.com.cert /etc/docker/certs.d/yourdomain.com/
   cp yourdomain.com.key /etc/docker/certs.d/yourdomain.com/
   cp ca.crt /etc/docker/certs.d/yourdomain.com/
   ```

   If you mapped the default `nginx` port 443 to a different port, create the folder `/etc/docker/certs.d/yourdomain.com:port`, or `/etc/docker/certs.d/harbor_IP:port`.

4. Restart Docker Engine.

   ```sh
   systemctl restart docker
   ```

You might also need to trust the certificate at the OS level. See [Troubleshooting Harbor Installation](https://goharbor.io/docs/2.8.0/install-config/troubleshoot-installation/#https) for more information.

The following example illustrates a configuration that uses custom certificates.

```fallback
/etc/docker/certs.d/
    └── yourdomain.com:port
       ├── yourdomain.com.cert  <-- Server certificate signed by CA
       ├── yourdomain.com.key   <-- Server key signed by CA
       └── ca.crt               <-- Certificate authority that signed the registry certificate
```

## Deploy or Reconfigure Harbor

If you have not yet deployed Harbor, see [Configure the Harbor YML File](https://goharbor.io/docs/2.8.0/install-config/configure-yml-file/) for information about how to configure Harbor to use the certificates by specifying the `hostname` and `https` attributes in `harbor.yml`.

If you already deployed Harbor with HTTP and want to reconfigure it to use HTTPS, perform the following steps.

1. Run the `prepare` script to enable HTTPS.

   Harbor uses an `nginx` instance as a reverse proxy for all services. You use the `prepare` script to configure `nginx` to use HTTPS. The `prepare` is in the Harbor installer bundle, at the same level as the `install.sh` script.

   ```sh
   ./prepare
   ```

2. If Harbor is running, stop and remove the existing instance.

   Your image data remains in the file system, so no data is lost.

   ```sh
   docker-compose down -v
   ```

3. Restart Harbor:

   ```sh
   docker-compose up -d
   ```

## Verify the HTTPS Connection

After setting up HTTPS for Harbor, you can verify the HTTPS connection by performing the following steps.

- Open a browser and enter [https://yourdomain.com](https://yourdomain.com/). It should display the Harbor interface.

  Some browsers might show a warning stating that the Certificate Authority (CA) is unknown. This happens when using a self-signed CA that is not from a trusted third-party CA. You can import the CA to the browser to remove the warning.

- On a machine that runs the Docker daemon, check the `/etc/docker/daemon.json` file to make sure that the `-insecure-registry` option is not set for [https://yourdomain.com](https://yourdomain.com/).

- Log into Harbor from the Docker client.

  ```sh
  docker login yourdomain.com
  ```

  If you’ve mapped `nginx` 443 port to a different port,add the port in the `login` command.

  ```sh
  docker login yourdomain.com:port
  ```

## What to Do Next

- If the verification succeeds, see [Harbor Administration](https://goharbor.io/docs/2.8.0/administration/) for information about using Harbor.
- If installation fails, see [Troubleshooting Harbor Installation](https://goharbor.io/docs/2.8.0/install-config/troubleshoot-installation/).





### Configure the Harbor YML File

You set system level parameters for Harbor in the `harbor.yml` file that is contained in the installer package. These parameters take effect when you run the `install.sh` script to install or reconfigure Harbor.

After the initial deployment and after you have started Harbor, you perform additional configuration in the Harbor Web Portal.

## Required Parameters

The table below lists the parameters that must be set when you deploy Harbor. By default, all of the required parameters are uncommented in the `harbor.yml` file. The optional parameters are commented with `#`. You do not necessarily need to change the values of the required parameters from the defaults that are provided, but these parameters must remain uncommented. At the very least, you must update the `hostname` parameter.

**IMPORTANT**: Harbor does not ship with any certificates. In versions up to and including 1.9.x, by default Harbor uses HTTP to serve registry requests. This is acceptable only in air-gapped test or development environments. In production environments, always use HTTPS. If you enable Content Trust with Notary to properly sign all images, you must use HTTPS.

You can use certificates that are signed by a trusted third-party CA, or you can use self-signed certificates. For information about how to create a CA, and how to use a CA to sign a server certificate and a client certificate, see [Configuring Harbor with HTTPS Access](https://goharbor.io/docs/2.8.0/install-config/configure-https/).

| Parameter               | Sub-parameters          | Description and Additional Parameters                        |
| :---------------------- | :---------------------- | :----------------------------------------------------------- |
| `hostname`              | None                    | Specify the IP address or the fully qualified domain name (FQDN) of the target host on which to deploy Harbor. This is the address at which you access the Harbor Portal and the registry service. For example, `192.168.1.10` or `reg.yourdomain.com`. The registry service must be accessible to external clients, so do not specify `localhost`, `127.0.0.1`, or `0.0.0.0` as the hostname. |
| `http`                  |                         | Do not use HTTP in production environments. Using HTTP is acceptable only in air-gapped test or development environments that do not have a connection to the external internet. Using HTTP in environments that are not air-gapped exposes you to man-in-the-middle attacks. |
|                         | `port`                  | Port number for HTTP, for both Harbor portal and Docker commands. The default is 80. |
| `https`                 |                         | Use HTTPS to access the Harbor Portal and the token/notification service. Always use HTTPS in production environments and environments that are not air-gapped. |
|                         | `port`                  | The port number for HTTPS, for both Harbor portal and Docker commands. The default is 443. |
|                         | `certificate`           | The path to the SSL certificate.                             |
|                         | `private_key`           | The path to the SSL key.                                     |
| `internal_tls`          |                         | Use HTTPS to communicate between harbor components           |
|                         | `enabled`               | Set this flag to `true` means internal tls is enabled        |
|                         | `dir`                   | The path to the directory that contains internal certs and keys |
| `harbor_admin_password` | None                    | Set an initial password for the Harbor system administrator. This password is only used on the first time that Harbor starts. On subsequent logins, this setting is ignored and the administrator's password is set in the Harbor Portal. The default username and password are `admin` and `Harbor12345`. |
| `database`              |                         | Use a local PostgreSQL database. You can optionally configure an external database, in which case you can deactivate this option. |
|                         | `password`              | Set the root password for the local database. You must change this password for production deployments. |
|                         | `max_idle_conns`        | The maximum number of connections in the idle connection pool. If it <=0, no idle connections are retained. |
|                         | `max_open_conns`        | The maximum number of open connections to the database. If it <= 0, then there is no limit on the number of open connections. |
|                         | `conn_max_lifetime`     | The maximum amount of time a connection may be reused. If it <= 0, connections are not closed due to a connection's age. |
|                         | `conn_max_idle_time`    | The maximum amount of time a connection may be idle. If it <= 0, connections are not closed due to a connection's idle time. |
| `data_volume`           | None                    | The location on the target host in which to store Harbor's data. This data remains unchanged even when Harbor's containers are removed and/or recreated. You can optionally configure external storage, in which case deactivate this option and enable `storage_service`. The default is `/data`. |
| `trivy`                 |                         | Configure Trivy scanner.                                     |
|                         | `ignore_unfixed`        | Set the flag to `true` to display only fixed vulnerabilities. The default value is `false` |
|                         | `security_check`        | Comma-separated list of what security issues to detect. Possible values are `vuln`, `config` and `secret`. Defaults to `vuln`. |
|                         | `skip_update`           | You might want to enable this flag in test or CI/CD environments to avoid GitHub rate limiting issues. If the flag is enabled you have to download the `trivy-offline.tar.gz` archive manually, extract and the `trivy.db` and `metadata.json` files and mount them in the `/home/scanner/.cache/trivy/db/trivy.db` path in container. The default value is `false` |
|                         | `insecure`              | Set the flag to `true` to skip verifying registry certificate. The default value is `false` |
|                         | `github_token`          | Set the GitHub access token to download Trivy DB. Trivy DB is downloaded by Trivy from the GitHub release page. Anonymous downloads from GitHub are subject to the limit of 60 requests per hour. Normally such rate limit is enough for production operations. If, for any reason, it's not enough, you could increase the rate limit to 5000 requests per hour by specifying the GitHub access token. For more details on GitHub rate limiting please consult https://developer.github.com/v3/#rate-limiting .You can create a GitHub token by following the instructions in https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line |
| `jobservice`            | `max_job_workers`       | The maximum number of replication workers in the job service. For each image replication job, a worker synchronizes all tags of a repository to the remote destination. Increasing this number allows more concurrent replication jobs in the system. However, since each worker consumes a certain amount of network/CPU/IO resources, set the value of this attribute based on the hardware resource of the host. The default is 10. |
| `notification`          | `webhook_job_max_retry` | Set the maximum number of retries for web hook jobs. The default is 10. |
| `log`                   |                         | Configure logging. Harbor uses `rsyslog` to collect the logs for each container. |
|                         | `level`                 | Set the logging level to `debug`, `info`, `warning`, `error`, or `fatal`. The default is `info`. |
|                         | `local`                 | Set the log retention parameters:`rotate_count`: Log files are rotated `rotate_count` times before being removed. If count is 0, old versions are removed rather than rotated. The default is 50.`rotate_size`: Log files are rotated only if they grow bigger than `rotate_size` bytes. Use `k` for kilobytes, `M` for megabytes, and `G` for gigabytes. `100`, `100k`, `100M` and `100G` are all valid values. The default is 200M.`location`: Set the directory in which to store the logs. The default is `/var/log/harbor`. |
|                         | `external_endpoint`     | Enable this option to forward logs to a syslog server.`protocol`: Transport protocol for the syslog server. Default is TCP.`host`: The URL of the syslog server.`port`: The port on which the syslog server listens |
| `proxy`                 |                         | Configure proxies to be used by trivy-adapter, the replication jobservice, and Harbor. Leave blank if no proxies are required. Some proxies have whitelist settings, if Trivy is enabled, you need to add the following urls to the proxy server whitelist: `github.com`, `github-releases.githubusercontent.com`, and `*.s3.amazonaws.com.` |
|                         | `http_proxy`            | Configure an HTTP proxy, for example, `http://my.proxy.com:3128`. |
|                         | `https_proxy`           | Configure an HTTPS proxy, for example, `http://my.proxy.com:3128`. |
|                         | `no_proxy`              | Configure when not to use a proxy, for example, `127.0.0.1,localhost,core,registry`. |
| `cache`                 |                         | Configure cache layer for your Harbor instance. When enabled, Harbor will cache some Harbor resources (for example, artifacts, projects, or project metadata) using Redis, reducing the amount of time and resources used for repeated requests for the same Harbor resource. It's strongly recommended that you enable this feature on Harbor instances with high concurrent pull request rates to improve Harbor's overall performance. For more details on the cache layer implementation and performance improvements, see the [Cache Layer wiki page](https://github.com/goharbor/perf/wiki/Cache-layer). |
|                         | `enabled`               | Default is `false`, set to `true` to enable Harbor's cache layer. |
|                         | `expire_hours`          | Configure the cache expiration limit in hours. Default is 24. |

## Optional Parameters

The following table lists the additional, optional parameters that you can set to configure your Harbor deployment beyond the minimum required settings. To enable a setting, you must uncomment it in `harbor.yml` by deleting the leading `#` character.

| Parameter           | Sub-Parameters         | Description and Additional Parameters                        |
| :------------------ | :--------------------- | :----------------------------------------------------------- |
| `external_url`      | None                   | Enable this option to use an external proxy. When enabled, the hostname is no longer used. |
|                     |                        |                                                              |
| `storage_service`   |                        | By default, Harbor stores images and charts on your local filesystem. In a production environment, you might want to use another storage backend instead of the local filesystem. The parameters listed below are the configurations for the registry. See *Configuring Storage Backend* below for more information about how to configure a different backend. |
|                     | `ca_bundle`            | The path to the custom root CA certificate, which is injected into the trust store of registry and chart repository containers. This is usually needed if internal storage uses a self signed certificate. |
|                     | `filesystem`           | The default is `filesystem`, but you can set `azure`, `gcs`, `s3`, `swift` and `oss`. For information about how to configure other backends, see [Configuring a Storage Backend](https://goharbor.io/docs/2.8.0/install-config/configure-yml-file/#backend) below. Set `maxthreads` to limit the number of threads to the external provider. The default is 100. |
|                     | `redirect`             | Set `deactivate` to `true` when you want to deactivate registry redirect |
| `external_database` |                        | Configure external database settings, if you deactivate the local database option. Currently, Harbor only supports PostgreSQL database. You must create three databases for Harbor core, Notary server, and Notary signer. The tables are generated automatically when Harbor starts up. |
|                     | `harbor`               | Configure an external database for Harbor data.`host`: Hostname of the Harbor database.`port`: Database port.`db_name`: Database name.`username`: Username to connect to the core Harbor database.`password`: Password for the account you set in `username`.`ssl_mode`: Enable SSL mode.`max_idle_conns`: The maximum number of connections in the idle connection pool. If <=0 no idle connections are retained. The default value is 2.`max_open_conns`: The maximum number of open connections to the database. If <= 0 there is no limit on the number of open connections. The default value is 0. |
|                     | `notary_signer`        | Configure an external database for the Notary signer database`host`: Hostname of the Notary signer database`port`: Database port.`db_name`: Database name.`username`: Username to connect to the Notary signer database.`password`: Password for the account you set in `username`.`ssl_mode`: Enable SSL mode. |
|                     | `notary_server`        | `host`: Hostname of the Notary server database.`port`: Database port.`db_name`: Database name.`username`: Username to connect to the Notary server database.`password`: Password for the account you set in `username`.`ssl_mode`: Enable SSL mode.e |
| `external_redis`    |                        | Configure an external Redis instance.                        |
|                     | `host`                 | redis_host:redis_port of the external Redis instance. If you are using Sentinel mode, this part should be host_sentinel1:port_sentinel1,host_sentinel2:port_sentinel2 |
|                     | `sentinel_master_set`  | Only set this when using Sentinel mode                       |
|                     | `password`             | Password to connect to the external Redis instance.          |
|                     | `registry_db_index`    | Database index for Harbor registry.                          |
|                     | `jobservice_db_index`  | Database index for jobservice.                               |
|                     | `chartmuseum_db_index` | Database index for Chart museum.                             |
|                     | `trivy_db_index`       | Database index for Trivy adapter.                            |
| `metric`            |                        | Configure exposing Harbor instance metrics to a specified port and path |
|                     | `enabled`              | Enable exposing metrics on your Harbor instance by setting this to `true`. Default is `false` |
|                     | `port`                 | Port metrics are exposed on. Default is `9090`               |
|                     | `path`                 | Path metrics are exposed on. Default is `/metrics`           |
| `trace`             |                        | Configure exposing Distributed tracing data                  |
|                     | `enabled`              | Enable exposing tracing on your Harbor instance by setting this to `true`. Default is `false` |
|                     | `sample_rate`          | Set the sample rate of tracing. For example, set sample_rate to `1` if you wanna sampling 100% of trace data; set `0.5` if you wanna sampling 50% of trace data, and so forth |
|                     | `namespace`            | Namespace used to differenciate different harbor services, which will set to attribute with key `service.namespace` |
|                     | `attributes`           | The attributes is a key value dict contains user defined customized attributes used to initialize trace provider, and all of these atributes will added to trace data |
|                     | `jaeger`               | `endpoint`: The url of endpoint(for example `http://127.0.0.1:14268/api/traces`). set endpoint means export to jaeger collector via http.`username:`: Username used to connect endpoint. Left empty if not needed.`password:`: Password used to connect endpoint. Left empty if not needed.`agent_host`: The host name of jaeger agent. Set agent_host means export data to jaeger agent via udp.`agent_port:`: The port name of jaeger agent. |
|                     | `otel`                 | `endpoint`: The hostname and port for otel compitable backend(for example `127.0.0.1:4318`).`url_path:`: The url path of endpoint(for example `127.0.0.1:4318`)`compression:`: If enabling data compression`insecure`: Ignore cert verification for otel backend`timeout:`: The timeout of data transfer |

The `harbor.yml` file includes options to configure a UAA CA certificate. This authentication mode is not recommended and is not documented.

## Configuring a Storage Backend

By default Harbor uses local storage for the registry, but you can optionally configure the `storage_service` setting so that Harbor uses external storage. For information about how to configure the storage backend of a registry for different storage providers, see the [Registry Configuration Reference](https://docs.docker.com/registry/configuration/#storage) in the Docker documentation. For example, if you use Openstack Swift as your storage backend, the parameters might resemble the following:

```yaml
storage_service:
  ca_bundle:
  swift:
    username: admin
    password: ADMIN_PASS
    authurl: http://keystone_addr:35357/v3/auth
    tenant: admin
    domain: default
    region: regionOne
    container: docker_images"
  redirect:
    disable: false
```

## What to Do Next

To install Harbor, [Run the Installer Script](https://goharbor.io/docs/2.8.0/install-config/run-installer-script/).





### Configure Internal TLS communication between Harbor Component

By default, The internal communication between Harbor’s component (harbor-core,harbor-jobservice,proxy,harbor-portal,registry,registryctl,trivy_adapter,chartmuseum) use HTTP protocol which might not be secure enough for some production environment. Since Harbor v2.0, TLS can be used for this internal network. In production environments, always use HTTPS is a recommended best practice.

This functionality is introduced via the `internal_tls` in `harbor.yml` file. To enabled internal TLS, set `enabled` to `true` and set the `dir` value to the path of directory that contains the internal cert files.

All certs can be automatically generated by `prepare` tool.

```bash
docker run -v /:/hostfs goharbor/prepare:v2.8.x gencert -p /path/to/internal/tls/cert
```

> Remember to replace the version number with the current version number.

User also can provide their own CA to generate the other certs. Just put certificate and key of the CA on internal tls cert directory and name them as `harbor_internal_ca.key` and `harbor_internal_ca.crt`. Besides, a user can also provide the certs for all components. However, there are some constraints for the certs:

- First, all certs must be signed by a single unique CA

- Second, the filename of the internal cert and `CN` field on cert file must follow the convention listed below’

- Third, because the self signed certificate without SAN was deprecated in Golang 1.5, you must add the SAN extension to your cert files when generating certs by yourself or the Harbor instance will not start up normally. The DNS name in SAN extension should the same as CN field in the table below. For more information please refer to

   

  golang 1.5 release notes

   

  and

   

  this issue

  .

  | name                     | usage                                  | CN              |
  | :----------------------- | :------------------------------------- | :-------------- |
  | `harbor_internal_ca.key` | ca’s key file for internal TLS         | N/A             |
  | `harbor_internal_ca.crt` | ca’s certificate file for internal TLS | N/A             |
  | `core.key`               | core’s key file                        | N/A             |
  | `core.crt`               | core’s certificate file                | `core`          |
  | `job_service.key`        | job_service’s key file                 | N/A             |
  | `job_service.crt`        | job_service’s certificate file         | `jobservice`    |
  | `proxy.key`              | proxy’s key file                       | N/A             |
  | `proxy.crt`              | proxy’s certificate file               | `proxy`         |
  | `portal.key`             | portal’s key file                      | N/A             |
  | `portal.crt`             | portal’s certificate file              | `portal`        |
  | `registry.key`           | registry’s key file                    | N/A             |
  | `registry.crt`           | registry’s certificate file            | `registry`      |
  | `registryctl.key`        | registryctl’s key file                 | N/A             |
  | `registryctl.crt`        | registryctl’s certificate file         | `registryctl`   |
  | `notary_server.key`      | notary_server’s key file               | N/A             |
  | `notary_server.crt`      | notary_server’s certificate file       | `notary-server` |
  | `notary_signer.key`      | notary_signer’s key file               | N/A             |
  | `notary_signer.crt`      | notary_signer’s certificate file       | `notary-signer` |
  | `trivy_adapter.key`      | trivy_adapter.’s key file              | N/A             |
  | `trivy_adapter.crt`      | trivy_adapter.’s certificate file      | `trivy-adapter` |





### Run the Installer Script

Once you have configured `harbor.yml` copied from `harbor.yml.tmpl` and optionally set up a storage backend, you install and start Harbor by using the `install.sh` script. Note that it might take some time for the online installer to download all of the Harbor images from Docker hub.

You can install Harbor in different configurations:

- Just Harbor, without Notary and Trivy
- Harbor with Notary
- Harbor with Trivy
- Harbor with Notary and Trivy

## Default installation without Notary and Trivy

The default Harbor installation does not include Notary or Trivy service. Run the following command

```sh
sudo ./install.sh
```

If the installation succeeds, you can open a browser to visit the Harbor interface at `http://reg.yourdomain.com`, changing `reg.yourdomain.com` to the hostname that you configured in `harbor.yml`. If you did not change them in `harbor.yml`, the default administrator username and password are `admin` and `Harbor12345`.

Log in to the admin portal and create a new project, for example, `myproject`. You can then use Docker commands to log in to Harbor, tag images, and push them to Harbor.

```sh
docker login reg.yourdomain.com
docker push reg.yourdomain.com/myproject/myrepo:mytag
```

- If your installation of Harbor uses HTTPS, you must provide the Harbor certificates to the Docker client. For information, see [Configure HTTPS Access to Harbor](https://goharbor.io/docs/2.8.0/install-config/run-installer-script/configure-https.md#provide-the-certificates-to-harbor-and-docker).
- If your installation of Harbor uses HTTP, you must add the option `--insecure-registry` to your client’s Docker daemon and restart the Docker service. For more information, see [Connecting to Harbor via HTTP](https://goharbor.io/docs/2.8.0/install-config/run-installer-script/#connect-http) below.

## Installation with Notary

To install Harbor with the Notary service, add the `--with-notary` parameter when you run `install.sh`:

```sh
sudo ./install.sh --with-notary
```

For installation with Notary, you must configure Harbor to use HTTPS.

For more information about Notary and Docker Content Trust, see [Content Trust](https://docs.docker.com/engine/security/trust/content_trust/) in the Docker documentation.

## Installation with Trivy

To install Harbor with Trivy service, add the `--with-trivy` parameter when you run `install.sh`:

```sh
sudo ./install.sh --with-trivy
```

For more information about Trivy, see the [Trivy documentation](https://github.com/aquasecurity/trivy). For more information about how to use Trivy in an webproxy environment see [Configure custom Certification Authorities for trivy](https://goharbor.io/docs/2.8.0/install-config/run-installer-script/administration/vulnerability-scanning/configure-custom-certs.md)

## Installation with Notary and Trivy

If you want to install both Notary and Trivy, specify all of the parameters in the same command:

```fallback
sudo ./install.sh --with-notary --with-trivy
```

## Connecting to Harbor via HTTP

**IMPORTANT:** If your installation of Harbor uses HTTP rather than HTTPS, you must add the option `--insecure-registry` to your client’s Docker daemon. By default, the daemon file is located at `/etc/docker/daemon.json`.

For example, add the following to your `daemon.json` file:

```
{
"insecure-registries" : ["myregistrydomain.com:5000", "0.0.0.0"]
}
```

After you update `daemon.json`, you must restart both Docker Engine and Harbor.

1. Restart Docker Engine.

   ```sh
   systemctl restart docker
   ```

2. Stop Harbor.

   ```sh
   docker-compose down -v
   ```

3. Restart Harbor.

   ```sh
   docker-compose up -d
   ```

## What to Do Next

- If the installation succeeds, see [Harbor Administration](https://goharbor.io/docs/2.8.0/administration/) for information about using Harbor.
- If you deployed Harbor with HTTP and you want to secure the connections to Harbor, see [Configure HTTPS Access to Harbor](https://goharbor.io/docs/2.8.0/install-config/configure-https/).
- If installation fails, see [Troubleshooting Harbor Installation](https://goharbor.io/docs/2.8.0/install-config/troubleshoot-installation/).

