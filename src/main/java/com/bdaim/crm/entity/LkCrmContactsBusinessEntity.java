package com.bdaim.crm.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_crm_contacts_business", schema = "", catalog = "")
public class LkCrmContactsBusinessEntity {
    private int id;
    private int businessId;
    private int contactsId;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "business_id")
    public int getBusinessId() {
        return businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    @Basic
    @Column(name = "contacts_id")
    public int getContactsId() {
        return contactsId;
    }

    public void setContactsId(int contactsId) {
        this.contactsId = contactsId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmContactsBusinessEntity that = (LkCrmContactsBusinessEntity) o;
        return id == that.id &&
                businessId == that.businessId &&
                contactsId == that.contactsId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, businessId, contactsId);
    }
}
