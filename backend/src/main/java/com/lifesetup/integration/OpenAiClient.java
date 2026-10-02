package com.lifesetup.integration;
import java.util.List;
public interface OpenAiClient {
 record Intent(String taskId,String kind) {}
 Intent interpret(String question,List<String> taskIds) throws Exception;
}
