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
          git(url: 'https://gitee.com/leixing1012/yygh-admin.git', credentialsId: 'gitee-leixing', branch: 'master', changelog: true, poll: false)
          sh 'ls -al'
        }
      }
    }

    stage('项目编译') {
      agent none
      steps {
        container('nodejs') {
          sh 'node -v'
          sh 'npm -v'
          sh 'npm i node-sass --sass_binary_site=https://npm.taobao.org/mirrors/node-sass/'
          sh 'npm install --registry=https://registry.npm.taobao.org'
          sh 'npm run build'
          sh 'ls'
        }
      }
    }

    stage('构建镜像') {
      agent none
      steps {
        container('nodejs') {
          sh 'ls -al'
          sh 'podman build -t yygh-admin:latest -f Dockerfile  .'
        }
      }
    }

    stage('推送镜像') {
      agent none
      steps {
        container('nodejs') {
          withCredentials([usernamePassword(credentialsId: 'harbor-robot-test', passwordVariable: 'REGISTRY_PASSWORD', usernameVariable: 'REGISTRY_USERNAME',)]) {
            sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
            sh 'podman tag yygh-admin:latest $REGISTRY/$REGISTRY_NAMESPACE/yygh-admin:SNAPSHOT-$BUILD_NUMBER'
            sh 'podman push $REGISTRY/$REGISTRY_NAMESPACE/yygh-admin:SNAPSHOT-$BUILD_NUMBER'
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
        mail(to: 'leixing1012@163.com', cc: 'leixing1012@qq.com', subject: 'project-test构建报告', body: "project-test-yygh-admin 构建报告：\n$BUILD_NUMBER 构建成功")
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
