package com.techelevator.tenmo.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This DTO contains details necessary for interfacing with the database's User table.
 *
 * @author Jayden Southworth, Kadeam Howell
 *
 */

public class User {

   private Long id;
   private String username;
   private String password;
   private boolean activated;
   private Set<Authority> authorities = new HashSet<>();

   public User() { }

   public User(Long id, String username, String password, String authorities) {
      this.id = id;
      this.username = username;
      this.password = password;
      this.activated = true;
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getUsername() {
      return username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public boolean isActivated() {
      return activated;
   }

   public void setActivated(boolean activated) {
      this.activated = activated;
   }

   public Set<Authority> getAuthorities() {
      return authorities;
   }

   public void setAuthorities(Set<Authority> authorities) {
      this.authorities = authorities;
   }

   /**
    * This method takes a comma-delimited String of roles,
    * splits them apart, adds them to a list, and then appends
    * 'ROLE_' to each item in the list.
    *
    * @param authorities Retrieves a serialized list String of authorities.
    *
    */
   public void setAuthorities(String authorities) {
      String[] roles = authorities.split(",");
      for(String role : roles) {
         this.authorities.add(new Authority("ROLE_" + role));
      }
   }

   /**
    * This method overrides the equals() method,
    * and checks the passed-in object to see if it's
    * a valid object.
    *
    * @return returns True or False depending on whether the object is valid.
    *
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      User user = (User) o;
      return id == user.id &&
              activated == user.activated &&
              Objects.equals(username, user.username) &&
              Objects.equals(password, user.password) &&
              Objects.equals(authorities, user.authorities);
   }

   /**
    * This method overrides the hashCode() method,
    * and hashes the User's details.
    *
    * @return returns a hashed object containing User details.
    *
    */
   @Override
   public int hashCode() {
      return Objects.hash(id, username, password, activated, authorities);
   }

   /**
    * This method overrides the toString() method when called from the
    * User DTO.
    *
    * @return returns serialized User details.
    *
    */
   @Override
   public String toString() {
      return "User{" +
              "id=" + id +
              ", username='" + username + '\'' +
              ", activated=" + activated +
              ", authorities=" + authorities +
              '}';
   }
}
