package jmutation.mutation.explainable.client;


public class ExplainableMutationClient {
    private final Client client;

    public ExplainableMutationClient(Client client) {
        this.client = client;
    }

    public String[] generate(String javadoc, String method) {
        client.sendMsg(javadoc.getBytes());
        byte[] buggyMethodSummary = client.receiveMsg();
        client.sendMsg(method.getBytes());
        byte[] fixedMethodSummary = client.receiveMsg();
        return new String[]{new String(buggyMethodSummary).replace("\0", ""),
                new String(fixedMethodSummary).replace("\0", "")};
    }
}
