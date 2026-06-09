package com.crm.system.client.hx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * msgHeader complex type�� Java �ࡣ
 *
 * <p>
 * ����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 *
 * <pre>
 * &lt;complexType name="msgHeader">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sourceSystemId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sourceSystemName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "msgHeader", propOrder = { "sourceSystemId", "sourceSystemName" })
public class MsgHeader {

	protected String sourceSystemId;
	protected String sourceSystemName;

	/**
	 * ��ȡsourceSystemId���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getSourceSystemId() {
		return sourceSystemId;
	}

	/**
	 * ����sourceSystemId���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setSourceSystemId(String value) {
		this.sourceSystemId = value;
	}

	/**
	 * ��ȡsourceSystemName���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getSourceSystemName() {
		return sourceSystemName;
	}

	/**
	 * ����sourceSystemName���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setSourceSystemName(String value) {
		this.sourceSystemName = value;
	}

}
