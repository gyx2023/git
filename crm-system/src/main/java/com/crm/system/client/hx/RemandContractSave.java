package com.crm.system.client.hx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * remandContractSave complex type�� Java �ࡣ
 *
 * <p>
 * ����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 *
 * <pre>
 * &lt;complexType name="remandContractSave">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="msgHeader" type="{http://service.oppo.sdunicomsi.com/}msgHeader" minOccurs="0"/>
 *         &lt;element name="remandContractBean" type="{http://service.oppo.sdunicomsi.com/}remandContractBean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remandContractSave", propOrder = { "msgHeader", "remandContractBean" })
public class RemandContractSave {

	protected MsgHeader msgHeader;
	protected RemandContractBean remandContractBean;

	/**
	 * ��ȡmsgHeader���Ե�ֵ��
	 *
	 * @return possible object is {@link MsgHeader }
	 *
	 */
	public MsgHeader getMsgHeader() {
		return msgHeader;
	}

	/**
	 * ����msgHeader���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link MsgHeader }
	 *
	 */
	public void setMsgHeader(MsgHeader value) {
		this.msgHeader = value;
	}

	/**
	 * ��ȡremandContractBean���Ե�ֵ��
	 *
	 * @return possible object is {@link RemandContractBean }
	 *
	 */
	public RemandContractBean getRemandContractBean() {
		return remandContractBean;
	}

	/**
	 * ����remandContractBean���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link RemandContractBean }
	 *
	 */
	public void setRemandContractBean(RemandContractBean value) {
		this.remandContractBean = value;
	}

}
