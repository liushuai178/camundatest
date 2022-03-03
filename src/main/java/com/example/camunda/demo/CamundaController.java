package com.example.camunda.demo;


import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author guoqing
 * @ClassName camundaController
 * @Description
 * @date 2022/2/24 10:49 上午
 * Copyright
 */
@RestController
@RequestMapping("/test")
public class CamundaController {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;


    /**
     * @Title 流程部署
     * @Description createDefinition
     * @return boolean
     * @throws
     */
    @GetMapping(value ="/createDefinition" )
    public  boolean  createDefinition(){
        String name = "请假审批";
        Deployment deployment = repositoryService.createDeployment()// 创建一个部署对象
                .name(name)// 添加部署名称
                .addClasspathResource("diagram_3.bpmn")
                .deploy();// 完成部署
        System.out.println("部署ID：" + deployment.getId());
        System.out.println("部署名称" + deployment.getName());
        return true;
    }

    /**
     * @Title 流程启动
     * @Description startInstance
     * @param key
     * @return boolean
     * @throws
     */
    @GetMapping(value = "/startInstance")
    public  boolean startInstance(String key){
        //根据key启动流程实例 这个KEY可以在数据库表 ACT_RE_PROCDEF 的KEY_字段查看到，当然也可以在流程文件中查到，或者通过查询接口查到。
        //开启流程 需要指定当前用户
        // 设置当前用户为操作人
        identityService.setAuthenticatedUserId("小明");
        //ProcessInstance simpleTest = runtimeService.startProcessInstanceByKey(key);

        //指定变量启动
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("initiator", "小明");
        variables.put("approve", "小红");
        ProcessInstance simpleTest = runtimeService.startProcessInstanceByKey(key, variables);
        System.out.println("流程实例的ProcessInstanceId: " + simpleTest.getId());
        System.out.println("流程实例的ProcessDefinitionId: " + simpleTest.getProcessDefinitionId());
        System.out.println("————————————————————————————————————————————————————");

        if (simpleTest != null && StringUtils.isNotBlank(simpleTest.getProcessDefinitionId())) {
            List<Task> list = taskService.createTaskQuery().processInstanceId(simpleTest.getProcessInstanceId())
                    .active()
                    .list();

            for (Task task:list){
                System.out.println("name:"+task.getName());
                System.out.println("id:"+task.getId());
                System.out.println("ProcessDefinitionId"+task.getProcessDefinitionId());
                System.out.println("taskDefinitionKey"+task.getTaskDefinitionKey());
                System.out.println("当前任务执行人:"+task.getAssignee());

            }

        }

        return true;
    }

    /**
     * @Title 获取流程
     * @Description getDefinitionList
     * @return java.util.List<org.camunda.bpm.engine.repository.ProcessDefinition>
     * @throws
     */
    @GetMapping("/getDefinitionList")
    public List<ProcessDefinition> getDefinitionList(){

        DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

        List<Deployment> list = deploymentQuery.list();
        for (Deployment deployment : list) {
            System.out.println("sss"+deployment.toString());
        }
        System.out.println("---------------------------------------------------------------------");
        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
                .latestVersion()//最新结果
                //添加查询条件
                //.processDefinitionName("流程定义的name")
                //.processDefinitionId("流程定义的id")
                //.processDefinitionKey("流程定义的key")
                //排序
                .orderByProcessDefinitionVersion().asc()
                //查询结果
                //.count() //返回结果集数量
                //.listPage(firstResult,  maxResults)//分页查询
                //.singleResult() //唯一结果
                .list();//总的结果集数量
        for(ProcessDefinition processDefinition:processDefinitionList){
            System.out.println("id:" +processDefinition.getId());
            System.out.println("name:" +processDefinition.getName());
            System.out.println("key:" +processDefinition.getKey());
            System.out.println("version:" +processDefinition.getVersion());
            System.out.println("resourceName:" +processDefinition.getDiagramResourceName());
        }

        return processDefinitionList;

    }




    /**
     * @Title 根据进程实例id获取当前流程审批人
     * @Description getActivityTask
     * @param processInstanceId
     * @return void
     * @throws
     */
    @RequestMapping("/getActivityTask")
    public void getActivityTask(String processInstanceId){

        List<Task> taskList = taskService.createTaskQuery().taskAssignee("小红").list();


        for (Task task:taskList){
            System.out.println("name:"+task.getName());
            System.out.println("id:"+task.getId());
            System.out.println("ProcessDefinitionId"+task.getProcessDefinitionId());
            System.out.println("taskDefinitionKey"+task.getTaskDefinitionKey());
            System.out.println("当前任务执行人:"+task.getAssignee());

        }

//        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
//        .taskAssignee("小明")//使用审批人查询
//        .processInstanceId(processInstanceId)//使用流程实例ID查询
//        .finished()
//        .list();
//        for (HistoricTaskInstance historicTaskInstance:historicTaskInstances) {
//            List<Comment> taskComments = taskService.getTaskComments(historicTaskInstance.getId();
//            System.out.println(historicTaskInstance.getId());
//            System.out.println(historicTaskInstance.getAssignee());
//            System.out.println(historicTaskInstance.getName());
//        }
    }

    /**
     * @Title 添加审批人
     * @Description candidateUser
     * @param taskId
     * @param userId
     * @return void
     * @throws
     */
    @GetMapping("/addCandidateUser")
    public void candidateUser(String taskId,String userId){
        taskService.addCandidateUser(taskId,userId);
        //taskService.addCandidateGroup(taskId,groupId); 添加组
        System.out.println("ok");
    }

    /**
     * @Title 审批
     * @Description completeTask
     * @param taskId
     * @param userId
     * @return void
     * @throws
     */
    @GetMapping("/completeTask")
    public void completeTask(String taskId,String userId,String processInstanceId){

        //添加审批人
        identityService.setAuthenticatedUserId(userId);
        //添加审批意见，可在Act_Hi_Comment里的message查询到
        //三个参数分别为待办任务ID,流程实例ID,审批意见
        taskService.createComment(taskId, processInstanceId, "审批通过");
        taskService.complete(taskId);

        System.out.println("ok");
    }

    /**
     * @Title 删除流程
     * @Description deleteProcessInstance
     * @param processInstanceId
     * @return void
     * @throws
     */
    @GetMapping("/deleteProcessInstance")
    public void deleteProcessInstance(String processInstanceId){

        runtimeService.deleteProcessInstance(processInstanceId,"删除测试");

        System.out.println("ok");
    }


}