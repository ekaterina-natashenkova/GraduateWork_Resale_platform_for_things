package ru.skypro.homework.dto;

import org.junit.jupiter.api.Test;
import ru.skypro.homework.model.dto.Register;
import ru.skypro.homework.model.enums.Role;
import ru.skypro.homework.model.dto.User;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void shouldCreateRoleWithValues() {
        // Given & When
        Role roleUser = Role.USER;
        Role roleAdmin = Role.ADMIN;

        // Then
        assertNotNull(roleUser);
        assertNotNull(roleAdmin);
        assertEquals("USER", roleUser.name());
        assertEquals("ADMIN", roleAdmin.name());
    }

    @Test
    void shouldHaveTwoEnumValues() {
        // When
        Role[] roles = Role.values();

        // Then
        assertEquals(2, roles.length);
        assertEquals(Role.USER, roles[0]);
        assertEquals(Role.ADMIN, roles[1]);
    }

    @Test
    void valueOfShouldReturnCorrectRole() {
        // When & Then
        assertEquals(Role.USER, Role.valueOf("USER"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
    }

    @Test
    void ordinalShouldBeCorrect() {
        // When & Then
        assertEquals(0, Role.USER.ordinal());
        assertEquals(1, Role.ADMIN.ordinal());
    }

    @Test
    void toStringShouldReturnName() {
        // When & Then
        assertEquals("USER", Role.USER.toString());
        assertEquals("ADMIN", Role.ADMIN.toString());
    }

    @Test
    void valuesMethodShouldReturnAllRoles() {
        // When
        Role[] values = Role.values();

        // Then
        assertEquals(2, values.length);
        assertArrayEquals(new Role[]{Role.USER, Role.ADMIN}, values);
    }

    /**
     * Интеграционные тесты для всех использований Role
     */

    @Test
    void roleShouldBeConsistentAcrossAllDTOs() {
        // Given
        Register register = new Register();
        User user = new User();

        // When
        register.setRole(Role.USER);
        user.setRole(Role.USER);

        // Then
        assertEquals(register.getRole(), user.getRole());
        assertEquals(Role.USER, register.getRole());
        assertEquals(Role.USER, user.getRole());
    }

    @Test
    void shouldUseBothRoleTypesInDifferentDTOs() {
        // Given
        Register register = new Register();
        User user = new User();

        // When
        register.setRole(Role.USER);
        user.setRole(Role.ADMIN);

        // Then
        assertEquals(Role.USER, register.getRole());
        assertEquals(Role.ADMIN, user.getRole());
        assertNotEquals(register.getRole(), user.getRole());
    }

    @Test
    void roleShouldWorkInAllContexts() {
        // Test that Role can be used in different scenarios
        assertTrue(canUseRoleInRegister());
        assertTrue(canUseRoleInUser());
    }

    private boolean canUseRoleInRegister() {
        try {
            Register register = new Register();
            register.setRole(Role.USER);
            return register.getRole() == Role.USER;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean canUseRoleInUser() {
        try {
            User user = new User();
            user.setRole(Role.ADMIN);
            return user.getRole() == Role.ADMIN;
        } catch (Exception e) {
            return false;
        }
    }

}