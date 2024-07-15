package com.vlat.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class MailParams {
    private String id;
    private String emailTo;
}
