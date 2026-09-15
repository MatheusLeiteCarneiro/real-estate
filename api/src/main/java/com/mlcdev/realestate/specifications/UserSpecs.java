package com.mlcdev.realestate.specifications;

import com.mlcdev.realestate.entities.Role;
import com.mlcdev.realestate.entities.User;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.util.ObjectUtils;

public class UserSpecs {

    private UserSpecs(){}

    public static PredicateSpecification<User> usernameContains(String username){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(username)){return null;}
            String pattern = "%" + username.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), pattern);

        };
    }

    public static PredicateSpecification<User> hasRole(Role role){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(role)){return null;}
            return criteriaBuilder.isMember(role, root.get("authorities"));
        };
    }

    public static PredicateSpecification<User> isActive(Boolean isActive) {
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(isActive)){return null;}
            return criteriaBuilder.equal(root.get("active"), isActive);

        };
    }
}
