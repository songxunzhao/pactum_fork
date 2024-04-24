ALTER TABLE client ADD negotiation_status_fields jsonb default '{}';

create unique index client_tag_uindex on client (tag);

insert into client(tag, name, negotiation_extra_fields, negotiation_status_fields) values ('walmart', 'Walmart', null, '{}') on conflict(tag) do nothing;
insert into client(tag, name, negotiation_extra_fields, negotiation_status_fields) values ('test', 'TEST', null, '{}') on conflict(tag) do nothing;
insert into client(tag, name, negotiation_extra_fields, negotiation_status_fields) values ('pactum', 'Pactum', null, '{}') on conflict(tag) do nothing;
insert into client(tag, name, negotiation_extra_fields, negotiation_status_fields) values ('larsen', 'Larsen', null, '{}') on conflict(tag) do nothing;
insert into client(tag, name, negotiation_extra_fields, negotiation_status_fields) values ('adcash', 'Adcash', null, '{}') on conflict(tag) do nothing;
insert into client(tag, name, negotiation_extra_fields, negotiation_status_fields) values ('wesco', 'Wesco', null, '{}') on conflict(tag) do nothing;

update client set negotiation_extra_fields='{"admin": {"model": [{"key": "publisherId", "label": "Publisher ID"}, {"key": "email", "label": "email"}, {"key": "total_earnings_30_days", "label": "Total Earnings"}, {"key": "current_ads", "label": "Current Ads"}], "terms": [{"key": "status", "label": "Status"}, {"key": "values_label", "label": "New Ads"}, {"key": "export_terms.payment_terms_days", "label": "Payment Terms Days"}, {"key": "export_terms.kickback_percentage", "label": "Kickback Percentage"}, {"key": "export_terms.kickback_duration_months", "label": "Kickback Duration Months"}], "summary": []}, "client": {"model": [], "terms": [], "summary": []}}' where tag='adcash';
update client set negotiation_status_fields='{"END": {"label": "end"}, "ONGOING": {"label": "Ongoing"}, "BEGINNING": {"label": "beginning"}, "GIVE_FEEDBACK": {"label": "give_feedback"}, "CHOOSE_INTEREST": {"label": "choose_interest"}, "AGREEMENT_REACHED": {"label": "Agreement reached"}, "START_NEGOTIATING": {"label": "start_negotiating"}, "ADD_TAGS": {"label": "add_tags"}}' where tag='adcash';

update client set negotiation_extra_fields='{"admin": {"model": [{"key": "supplier_company_name", "label": "Supplier"}, {"key": "cogs_2019", "label": "COGS 2019"}, {"key": "link_to_contract", "label": "Contract"}, {"key": "negotiation_status", "label": "Status"}, {"key": "platform", "label": "Platform"}], "terms": [{"key": "chatLastUpdateTime", "label": "Updated"}, {"key": "status", "label": "Dynamic Status"}, {"key": "agreed_value_text", "label": "Improvement"}, {"key": "agreed_value_absolute_text", "label": "Improved value"}], "summary": [{"key": "agreed_value_text", "type": "TEXT", "label": "New deals reached", "operation": "COUNT"}, {"key": "agreed_value_text", "type": "PERCENT", "label": "Average improvement", "operation": "AVE"}, {"key": "agreed_value_absolute_text", "type": "CURRENCY", "label": "Average improved value", "operation": "AVE"}, {"key": "agreed_value_absolute_text", "type": "CURRENCY", "label": "Total improved value", "operation": "SUM"}]}, "client": {"model": [{"key": "supplier_company_name", "label": "Supplier"}, {"key": "cogs_2019", "label": "COGS 2019"}, {"key": "link_to_contract", "label": "Contract"}, {"key": "negotiation_status", "label": "Status"}, {"key": "platform", "label": "Platform"}], "terms": [{"key": "chatLastUpdateTime", "label": "Updated"}, {"key": "agreed_value_text", "label": "Improvement"}, {"key": "agreed_value_absolute_text", "label": "Improved value"}], "summary": [{"key": "agreed_value_text", "type": "TEXT", "label": "New deals reached", "operation": "COUNT"}, {"key": "agreed_value_text", "type": "PERCENT", "label": "Average improvement", "operation": "AVE"}, {"key": "agreed_value_absolute_text", "type": "CURRENCY", "label": "Average improved value", "operation": "AVE"}, {"key": "agreed_value_absolute_text", "type": "CURRENCY", "label": "Total improved value", "operation": "SUM"}]}}' where tag='wesco';
update client set negotiation_status_fields='{"ONGOING": {"label": "Ongoing"}, "NEW_PERSON_GIVEN": {"label": "New person given"}, "NO_AUTHORIZATION": {"label": "No authorization"}, "AGREEMENT_REACHED": {"label": "Agreement reached"}, "SIGNED": {"label": "Signed"}, "DECLINED": {"label": "Declined"}, "BLOCKED": {"label": "Blocked"}, "WAITING_FOR_CONTRACT_CONFIRMATION": {"label": "Waiting for contact confirmation"}, "TEMPORARY_BLOCKED": {"label": "Temporary blocked"}, "INITIATED": {"label": "Initiated"}, "CONTACT_CONFIRMED": {"label": "Contact confirmed"}, "NEW_CONTACT_RECEIVED": {"label": "New contact received"}, "WAITING_FOR_CORRECT_CONTRACT": {"label": "Waiting for correct contact"}, "NO_RESPONSE": {"label": "No response"}, "WRONG_CONTACT": {"label": "Wrong contact"}}' where tag='wesco';

update negotiation set status='END' where terms->>'status' = 'end';
update negotiation set status='ONGOING' where terms->>'status' = 'Ongoing';
update negotiation set status='BEGINNING' where terms->>'status' = 'beginning';
update negotiation set status='GIVE_FEEDBACK' where terms->>'status' = 'give_feedback';
update negotiation set status='CHOOSE_INTEREST' where terms->>'status' = 'choose_interest';
update negotiation set status='AGREEMENT_REACHED' where terms->>'status' = 'Agreement reached';
update negotiation set status='START_NEGOTIATING' where terms->>'status' = 'start_negotiating';
update negotiation set status='ADD_TAGS' where terms->>'status' = 'add_tags';

update negotiation set status='ONGOING' where model_attributes->>'negotiation_status' = 'Ongoing';
update negotiation set status='NEW_PERSON_GIVEN' where model_attributes->>'negotiation_status' = 'New person given';
update negotiation set status='NO_AUTHORIZATION' where model_attributes->>'negotiation_status' = 'No authorization';
update negotiation set status='AGREEMENT_REACHED' where model_attributes->>'negotiation_status' = 'Agreement reached';
update negotiation set status='SIGNED' where model_attributes->>'negotiation_status' = 'Signed';
update negotiation set status='DECLINED' where model_attributes->>'negotiation_status' = 'Declined';
update negotiation set status='BLOCKED' where model_attributes->>'negotiation_status' = 'Blocked';
update negotiation set status='WAITING_FOR_CONTRACT_CONFIRMATION' where model_attributes->>'negotiation_status' = 'Waiting for contact confirmation';
update negotiation set status='TEMPORARY_BLOCKED' where model_attributes->>'negotiation_status' = 'Temporary blocked';
update negotiation set status='INITIATED' where model_attributes->>'negotiation_status' = 'Initiated';
update negotiation set status='CONTACT_CONFIRMED' where model_attributes->>'negotiation_status' = 'Contact confirmed';
update negotiation set status='NEW_CONTACT_RECEIVED' where model_attributes->>'negotiation_status' = 'New contact received';
update negotiation set status='WAITING_FOR_CORRECT_CONTRACT' where model_attributes->>'negotiation_status' = 'Waiting for correct contact';
update negotiation set status='NO_RESPONSE' where model_attributes->>'negotiation_status' = 'No response';
update negotiation set status='WRONG_CONTACT' where model_attributes->>'negotiation_status' = 'Wrong contact';

update negotiation set status = NULL where status = 'CREATED';
update negotiation set status = NULL where status = 'OPENED';