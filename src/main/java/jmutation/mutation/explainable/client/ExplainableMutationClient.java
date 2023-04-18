package jmutation.mutation.explainable.client;


public class ExplainableMutationClient {
    private final Client client;

    public ExplainableMutationClient(Client client) {
        this.client = client;
    }

    public String[] generate(String javadoc, String method) {
        client.sendMsg(javadoc.getBytes());
        client.sendMsg(method.getBytes());
        byte[] mutatedComment = client.receiveMsg();
        byte[] mutatedMethod = client.receiveMsg();
        return new String[]{new String(mutatedComment).replace("\0", ""),
                new String(mutatedMethod).replace("\0", "")};
    }
}
