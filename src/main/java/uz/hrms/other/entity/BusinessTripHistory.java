package uz.hrms.other;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uz.hrms.other.entity.BaseEntity;
import uz.hrms.other.entity.UserAccount;

@Entity
@Table(schema = "hr", name = "business_trip_history")
public class BusinessTripHistory extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "business_trip_id", nullable = false)
	private BusinessTrip businessTrip;

	@Column(name = "action_type", nullable = false, length = 50)
	private String actionType;

	@Column(name = "status_from", length = 30)
	private String statusFrom;

	@Column(name = "status_to", length = 30)
	private String statusTo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor_user_id")
	private UserAccount actorUser;

	@Column(name = "comment_text", length = 1000)
	private String commentText;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "payload_json")
	private String payloadJson;

	public BusinessTrip getBusinessTrip() { return businessTrip; }
	public void setBusinessTrip(BusinessTrip businessTrip) { this.businessTrip = businessTrip; }
	public String getActionType() { return actionType; }
	public void setActionType(String actionType) { this.actionType = actionType; }
	public String getStatusFrom() { return statusFrom; }
	public void setStatusFrom(String statusFrom) { this.statusFrom = statusFrom; }
	public String getStatusTo() { return statusTo; }
	public void setStatusTo(String statusTo) { this.statusTo = statusTo; }
	public UserAccount getActorUser() { return actorUser; }
	public void setActorUser(UserAccount actorUser) { this.actorUser = actorUser; }
	public String getCommentText() { return commentText; }
	public void setCommentText(String commentText) { this.commentText = commentText; }
	public String getPayloadJson() { return payloadJson; }
	public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
}

