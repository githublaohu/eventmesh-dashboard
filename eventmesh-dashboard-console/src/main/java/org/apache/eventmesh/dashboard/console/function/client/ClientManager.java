package org.apache.eventmesh.dashboard.console.function.client;

import org.apache.eventmesh.dashboard.console.function.client.create.RedisClientCreateOperation;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {

    private static final ClientManager INSTANCE = new ClientManager();


    public static final ClientManager getInstance(){
        return INSTANCE;
    }


    private Map<ClientTypeEnums , Map<String, Object>> clientMap = new ConcurrentHashMap<>();

    private Map<ClientTypeEnums, ClientCreateOperation> clientCreateOperationMap = new ConcurrentHashMap<>();

    {
        for(ClientTypeEnums clientTypeEnums : ClientTypeEnums.values()){
            clientMap.put(clientTypeEnums,new ConcurrentHashMap<>());
        }

        clientCreateOperationMap.put(ClientTypeEnums.STORAGE_REDIS , new RedisClientCreateOperation());

    }

    private ClientManager(){}



    public <T>T createClient(){

        Map<String, Object> clients = this.clientMap.get(null);

        Object client = clients.get("");
        if(Objects.isNull(client)){
            ClientCreateOperation clientCreateOperation = this.clientCreateOperationMap.get(null);
            client = clientCreateOperation.createClient();
            clients.put("",client);
        }
        return (T)client;
    }

    public void deleteClient(){
        Map<String, Object> clients = this.clientMap.get(null);
        clients.remove("");
    }

}
