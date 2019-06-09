package spring.boot.apachecamel.model.family;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Child {
    private String firstName;
    private String lastName;
    private Gender gender;
}
