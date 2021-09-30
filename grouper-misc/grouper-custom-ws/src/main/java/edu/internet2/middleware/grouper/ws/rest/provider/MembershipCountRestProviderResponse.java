package edu.internet2.middleware.grouper.ws.rest.provider;

public class MembershipCountRestProviderResponse {
    private int immediate;
    private int effective;

    public MembershipCountRestProviderResponse(int immediate, int effective) {
        this.immediate = immediate;
        this.effective = effective;
    }

    public int getImmediate() {
        return immediate;
    }

    public void setImmediate(int immediate) {
        this.immediate = immediate;
    }

    public int getEffective() {
        return effective;
    }

    public void setEffective(int effective) {
        this.effective = effective;
    }
}
