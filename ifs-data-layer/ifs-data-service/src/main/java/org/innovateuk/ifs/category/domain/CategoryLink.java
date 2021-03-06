package org.innovateuk.ifs.category.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.DiscriminatorOptions;

import javax.persistence.*;

/**
 * Links an entity to a Category.
 * @param <T> the type of entity
 * @param <C> the type of Category to link to
 */
@Entity
@DiscriminatorColumn(name = "class_name")
@DiscriminatorOptions(force = true)
public abstract class CategoryLink<T, C extends Category> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(targetEntity = Category.class)
    @JoinColumn(name="categoryId", referencedColumnName="id")
    private C category;

    @Column(name="class_name", insertable = false, updatable = false)
    private String className;

    CategoryLink() {
    }

    protected CategoryLink(C category) {
        if (category == null) {
            throw new NullPointerException("category cannot be null");
        }
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public C getCategory() {
        return category;
    }

    public String getClassName() {
        return className;
    }

    public abstract T getEntity();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CategoryLink<?, ?> that = (CategoryLink<?, ?>) o;

        return new EqualsBuilder()
                .append(category, that.category)
                .append(className, that.className)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(category)
                .append(className)
                .toHashCode();
    }
}
