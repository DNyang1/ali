package com.finalProject.ali.product.sheet.status;

public final class SheetStatus {
    private SheetStatus() {}
    public static final String DRAFT = "DRAFT";
    public static final String SENT = "SENT";
    public static final String PAID_WAIT = "PAID_WAIT";
    public static final String PAID = "PAID";
    public static final String REJECTED = "REJECTED";
    public static final String CANCELLED = "CANCELLED";

    public static boolean canEditOrSend(String st) {
        return DRAFT.equals(st) || REJECTED.equals(st);
    }
    public static boolean canCancel(String st) {
        return DRAFT.equals(st) || REJECTED.equals(st) || SENT.equals(st);
    }
}
