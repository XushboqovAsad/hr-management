package uz.hrms.other.entity;


@Entity
@Table(name = "permissions", schema = "auth")
public class Permission extends BaseEntity {

    @Column(name = "module_code", nullable = false)
    private String moduleCode;

    @Column(name = "action_code", nullable = false)
    private String actionCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String authority() {
        return moduleCode + ":" + actionCode;
    }
}
