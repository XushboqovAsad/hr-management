package uz.hrms.other.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import uz.hrms.other.BaseEntity;
import uz.hrms.other.UserAccount;

import java.time.LocalDate;

@Entity
@Table(schema = "hr", name = "employees")
public class Employee extends BaseEntity {

    @Column(name = "personnel_number", nullable = false, length = 50)
    private String personnelNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "dismissal_date")
    private LocalDate dismissalDate;

    @Column(name = "status", nullable = false, length = 30)
    private String employmentStatus;

    public String getPersonnelNumber() {
        return personnelNumber;
    }

    public void setPersonnelNumber(String personnelNumber) {
        this.personnelNumber = personnelNumber;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public LocalDate getDismissalDate() {
        return dismissalDate;
    }

    public void setDismissalDate(LocalDate dismissalDate) {
        this.dismissalDate = dismissalDate;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getStatus() {
        return employmentStatus;
    }

    public void setStatus(String status) {
        this.employmentStatus = status;
    }
}
