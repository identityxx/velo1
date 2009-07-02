package velo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author Asaf Shakarchi
 *
 * An extension to the standard persistence action but maintains sequence
 */
//@MappedSuperclass
@Entity
@Table(name="VL_ACTION_DEF")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name="ActionIdSeq",sequenceName="ACTION_ID_SEQ")
public abstract class SequencedAction extends PersistenceAction implements Comparable<SequencedAction> {
	private Long id;
	private Integer sequence;
	private Boolean showStopper;

	public SequencedAction(String name, String description, Boolean active, Boolean showStopper, Integer sequence) {
		super(name, description, active);
		setSequence(sequence);
		setShowStopper(showStopper);
	}
	
	public SequencedAction() {
		
	}
	
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="ActionIdSeq")
    @Column(name="ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the sequence
	 */
	@Column(name="SEQUENCE", nullable=false)
	public Integer getSequence() {
		return sequence;
	}

	/**
	 * @param sequence the sequence to set
	 */
	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}


	/**
	 * @return the showStopper
	 */
	@Column(name="SHOW_STOPPER", nullable=false)
	public Boolean getShowStopper() {
		return showStopper;
	}


	/**
	 * @param showStopper the showStopper to set
	 */
	public void setShowStopper(Boolean showStopper) {
		this.showStopper = showStopper;
	}
	
	
	
	public int compareTo(SequencedAction sa) {
		 if (this.sequence == sa.getSequence())
	            return 0;
	        else if ((this.getSequence() > sa.getSequence()))
	            return 1;
	        else
	            return -1;
	}
}
