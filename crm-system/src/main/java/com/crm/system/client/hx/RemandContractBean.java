package com.crm.system.client.hx;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * remandContractBean complex type�� Java �ࡣ
 *
 * <p>
 * ����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 *
 * <pre>
 * &lt;complexType name="remandContractBean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="remandCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="contractBean" type="{http://service.oppo.sdunicomsi.com/}contractBean" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remandContractBean", propOrder = { "remandCode", "contractList" })
public class RemandContractBean {

	protected String remandCode;
	protected RemandContractBean.ContractList contractList;

	/**
	 * ��ȡremandCode���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getRemandCode() {
		return remandCode;
	}

	/**
	 * ����remandCode���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setRemandCode(String value) {
		this.remandCode = value;
	}

	/**
	 * ��ȡcontractList���Ե�ֵ��
	 *
	 * @return possible object is {@link RemandContractBean.ContractList }
	 *
	 */
	public RemandContractBean.ContractList getContractList() {
		return contractList;
	}

	/**
	 * ����contractList���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link RemandContractBean.ContractList }
	 *
	 */
	public void setContractList(RemandContractBean.ContractList value) {
		this.contractList = value;
	}

	/**
	 * <p>
	 * anonymous complex type�� Java �ࡣ
	 *
	 * <p>
	 * ����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
	 *
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="contractBean" type="{http://service.oppo.sdunicomsi.com/}contractBean" maxOccurs="unbounded" minOccurs="0"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 *
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "contractBean" })
	public static class ContractList {

		protected List<ContractBean> contractBean;

		/**
		 * Gets the value of the contractBean property.
		 *
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list will
		 * be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the contractBean property.
		 *
		 * <p>
		 * For example, to add a new item, do as follows:
		 *
		 * <pre>
		 * getContractBean().add(newItem);
		 * </pre>
		 *
		 *
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link ContractBean }
		 *
		 *
		 */
		public List<ContractBean> getContractBean() {
			if (contractBean == null) {
				contractBean = new ArrayList<ContractBean>();
			}
			return this.contractBean;
		}

	}

}
