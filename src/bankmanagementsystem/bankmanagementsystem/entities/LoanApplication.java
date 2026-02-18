package bankmanagementsystem.entities;

import java.math.BigDecimal;

public class LoanApplication {
    public String appId, accNo, type, uniName, propAddr, kycPath, incomePath, appliedBy;
    public BigDecimal amount;
    public int tenure;
    public double emi, interestRate;
}


