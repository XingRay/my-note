pipeline {
  agent {
    node {
      label 'maven'
    }

  }
  stages {
    stage('拉取代码') {
      agent none
      steps {
        container('maven') {
          git(url: 'https://gitee.com/leixing1012/yygh-parent.git', credentialsId: 'gitee-leixing', branch: 'master', changelog: true, poll: false)
          sh 'ls -al'
        }

      }
    }

    stage('单元测试') {
      agent none
      steps {
        container('maven') {
          sh 'ls -al'
        }

      }
    }

    stage('源码编译') {
      agent none
      steps {
        container('maven') {
          sh 'mvn -Dmaven.test.skip=true clean package'
          sh 'ls -al'
        }

      }
    }

    stage('制作镜像') {
      agent none
      steps {
        container('maven') {
          sh '''
                    cd hospital-manage
                    podman build -t hospital-manage:latest -f Dockerfile .
                    cd ..
                    '''
          sh '''
                    cd server-gateway
                    podman build -t server-gateway:latest -f Dockerfile .
                    cd ..
                    '''
          sh '''
                    cd service/service-cmn
                    podman build -t service-cmn:latest -f Dockerfile .
                    cd ../..
                    '''
          sh '''
                    cd service/service-hosp
                    podman build -t service-hosp:latest -f Dockerfile .
                    cd ../..
                    '''
          sh '''
                    cd service/service-order
                    podman build -t service-order:latest -f Dockerfile .
                    cd ../..
                    '''
          sh '''
                    cd service/service-oss
                    podman build -t service-oss:latest -f Dockerfile .
                    cd ../..
                    '''
          sh '''
                    cd service/service-sms
                    podman build -t service-sms:latest -f Dockerfile .
                    cd ../..
                    '''
          sh '''
                    cd service/service-statistics
                    podman build -t service-statistics:latest -f Dockerfile .
                    cd ../..
                    '''
          sh '''
                    cd service/service-task
                    podman build -t service-task:latest -f Dockerfile .
                    cd ../..
                    '''
          sh '''
                    cd service/service-user
                    podman build -t service-user:latest -f Dockerfile .
                    cd ../..
                    '''
        }

      }
    }

    stage('推送镜像') {
      agent none
      steps {
        container('maven') {
          withCredentials([usernamePassword(credentialsId: 'harbor-robot-test', passwordVariable: 'REGISTRY_PASSWORD', usernameVariable: 'REGISTRY_USERNAME',)]) {
            sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
            sh '''
                        podman tag  hospital-manage:latest $REGISTRY/$REGISTRY_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER
                        podman push  $REGISTRY/$REGISTRY_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER
                        '''
            sh '''
                        podman tag  server-gateway:latest $REGISTRY/$REGISTRY_NAMESPACE/server-gateway:SNAPSHOT-$BUILD_NUMBER
                        podman push  $REGISTRY/$REGISTRY_NAMESPACE/server-gateway:SNAPSHOT-$BUILD_NUMBER
                        '''
            sh '''
                        podman tag  service-cmn:latest $REGISTRY/$REGISTRY_NAMESPACE/service-cmn:SNAPSHOT-$BUILD_NUMBER
                        podman push  $REGISTRY/$REGISTRY_NAMESPACE/service-cmn:SNAPSHOT-$BUILD_NUMBER
                        '''
            sh '''
                        podman tag  service-hosp:latest $REGISTRY/$REGISTRY_NAMESPACE/service-hosp:SNAPSHOT-$BUILD_NUMBER
                        podman push  $REGISTRY/$REGISTRY_NAMESPACE/service-hosp:SNAPSHOT-$BUILD_NUMBER
                        '''
            sh '''
                        podman tag  service-order:latest $REGISTRY/$REGISTRY_NAMESPACE/service-order:SNAPSHOT-$BUILD_NUMBER
                        podman push  $REGISTRY/$REGISTRY_NAMESPACE/service-order:SNAPSHOT-$BUILD_NUMBER
                        '''
            sh '''
                        podman tag  service-oss:latest $REGISTRY/$REGISTRY_NAMESPACE/service-oss:SNAPSHOT-$BUILD_NUMBER
                        podman push  $REGISTRY/$REGISTRY_NAMESPACE/service-oss:SNAPSHOT-$BUILD_NUMBER
                        '''
            sh '''
                        podman tag service-sms:latest $REGISTRY/$REGISTRY_NAMESPACE/service-sms:SNAPSHOT-$BUILD_NUMBER
                        podman push $REGISTRY/$REGISTRY_NAMESPACE/service-sms:SNAPSHOT-$BUILD_NUMBER
                        '''
            sh '''
                        podman tag  service-statistics:latest $REGISTRY/$REGISTRY_NAMESPACE/service-statistics:SNAPSHOT-$BUILD_NUMBER
                        podman push  $REGISTRY/$REGISTRY_NAMESPACE/service-statistics:SNAPSHOT-$BUILD_NUMBER
                        '''
            sh '''
                        podman tag  service-task:latest $REGISTRY/$REGISTRY_NAMESPACE/service-task:SNAPSHOT-$BUILD_NUMBER
                        podman push  $REGISTRY/$REGISTRY_NAMESPACE/service-task:SNAPSHOT-$BUILD_NUMBER
                        '''
            sh '''
                        podman tag  service-user:latest $REGISTRY/$REGISTRY_NAMESPACE/service-user:SNAPSHOT-$BUILD_NUMBER
                        podman push  $REGISTRY/$REGISTRY_NAMESPACE/service-user:SNAPSHOT-$BUILD_NUMBER
                        '''
          }

        }

      }
    }

    stage('部署到生产环境') {
      agent none
      steps {
        container('maven') {
          withCredentials([kubeconfigFile(credentialsId: env.KUBECONFIG_CREDENTIAL_ID, variable: 'KUBECONFIG')]) {
            sh 'envsubst < hospital-manage/deploy/deploy.yml | kubectl apply -f -'
            sh 'envsubst < server-gateway/deploy/deploy.yml | kubectl apply -f -'
            sh 'envsubst < service/service-cmn/deploy/deploy.yml | kubectl apply -f -'
            sh 'envsubst < service/service-hosp/deploy/deploy.yml | kubectl apply -f -'
            sh 'envsubst < service/service-order/deploy/deploy.yml | kubectl apply -f -'
            sh 'envsubst < service/service-oss/deploy/deploy.yml | kubectl apply -f -'
            sh 'envsubst < service/service-sms/deploy/deploy.yml | kubectl apply -f -'
            sh 'envsubst < service/service-statistics/deploy/deploy.yml | kubectl apply -f -'
            sh 'envsubst < service/service-task/deploy/deploy.yml | kubectl apply -f -'
            sh 'envsubst < service/service-user/deploy/deploy.yml | kubectl apply -f -'
          }

        }

      }
    }

    stage('发送邮件') {
      agent none
      steps {
        mail(to: 'leixing1012@163.com', cc: 'leixing1012@qq.com', subject: 'project-test-yygh-parent 构建报告', body: "project-test构建报告：\n$BUILD_NUMBER 构建成功")
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
  parameters {
    string(name: 'TAG_NAME', defaultValue: '', description: '')
  }
}