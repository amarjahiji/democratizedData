package io.democratizedData.APIGateway;

public class UserInfo {
    private String gender;
    private Integer age;

    public UserInfo() {}

    public UserInfo(String gender, Integer age) {
        this.gender = gender;
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}