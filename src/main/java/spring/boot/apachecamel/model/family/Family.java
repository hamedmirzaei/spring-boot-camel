package spring.boot.apachecamel.model.family;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Family {
    private Dad dad;
    private Mom mom;
    private List<Child> children;
}
