# leetcode插件设置

settings/Tools/Leetcode Plugin



- [x] Custom Template



Code FileName:

```java
Question$!velocityTool.leftPadZeros($!{question.frontendQuestionId},3)
```



Code Template

```java
package leetcode.editor.cn;

${question.content}

public class Question$!velocityTool.leftPadZeros($!{question.frontendQuestionId},3){
    public static void main(String[] args) {
        Solution$!velocityTool.leftPadZeros($!{question.frontendQuestionId},3) solution = new Solution$!velocityTool.leftPadZeros($!{question.frontendQuestionId},3)();
    }
}

//class Solution$!velocityTool.leftPadZeros($!{question.frontendQuestionId},3) {
${question.code}
```

