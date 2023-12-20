pipeline {
  agent {
    node {
      label 'nodejs'
    }

  }
  stages {
    stage('拉取代码') {
      agent none
      steps {
        container('nodejs') {
          git(url: 'https://gitee.com/leixing1012/yygh-site.git', credentialsId: 'gitee-leixing', branch: 'master', changelog: true, poll: false)
          sh 'ls -al'
        }
      }
    }

    stage('构建镜像') {
      agent none
      steps {
        container('nodejs') {
          sh 'ls -al'
          sh 'podman -v'
          sh 'podman build -t yygh-site:latest -f Dockerfile  .'
        }

      }
    }

    stage('推送镜像') {
      agent none
      steps {
        container('nodejs') {
          withCredentials([usernamePassword(credentialsId: 'harbor-robot-test', passwordVariable: 'REGISTRY_PASSWORD', usernameVariable: 'REGISTRY_USERNAME',)]) {
            sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
            sh 'podman tag yygh-site:latest $REGISTRY/$REGISTRY_NAMESPACE/yygh-site:SNAPSHOT-$BUILD_NUMBER'
            sh 'podman push  $REGISTRY/$REGISTRY_NAMESPACE/yygh-site:SNAPSHOT-$BUILD_NUMBER'
          }
        }
      }
    }

    stage('部署到生产环境') {
      agent none
      steps {
        container('nodejs') {
          withCredentials([kubeconfigFile(credentialsId: env.KUBECONFIG_CREDENTIAL_ID, variable: 'KUBECONFIG')]) {
            sh 'envsubst < deploy/deploy.yml | kubectl apply -f -'
          }
        }
      }
    }

    stage('发送邮件') {
      agent none
      steps {
        mail(to: 'leixing1012@163.com', cc: 'leixing1012@qq.com', subject: 'project-test构建报告', body: "project-test-yygh-site 构建报告：\n$BUILD_NUMBER 构建成功")
      }
    }
  }

  environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITHUB_CREDENTIAL_ID = 'github-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    GITHUB_ACCOUNT = 'kubesphere'
    REGISTRY = '192.168.0.112:30003'
    REGISTRY_NAMESPACE = 'project-test'
  }
}
