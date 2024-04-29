package dev.codescreen.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Amount {
    private String amount;
    private String currency;
    private String debitOrCredit;
}
