package uz.hrms.other;

public final class DismissalDomain {
    private DismissalDomain() {    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dismissal_request_id", nullable = false)
    private uz.hrms.other.entity.DismissalRequest dismissalRequest;

    }
}

