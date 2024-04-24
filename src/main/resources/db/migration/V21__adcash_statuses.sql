-- Create a new client for Adcash POC
INSERT INTO client (tag, name)
VALUES ('adcashPoc', 'Adcash POC');

-- Update client config with proper status fields for both clients
UPDATE client
SET negotiation_status_fields = '{
  "NEGOTIATION_CREATED": {
    "label": "Negotiation created"
  },
  "MESSAGE_CLOSED": {
    "label": "Message closed"
  },
  "NEGOTIATION_STARTED": {
    "label": "Negotiation started"
  },
  "NEGOTIATION_INTEREST_INDICATED": {
    "label": "Negotiation interest indicated"
  },
  "NEGOTIATION_TRADING_STARTED": {
    "label": "Negotiation trading started"
  },
  "AGREEMENT_REACHED": {
    "label": "Agreement reached"
  },
  "AGREEMENT_NOT_REACHED": {
    "label": "Agreement not reached"
  },
  "OTHER": {
    "label": "Other"
  }
}'
WHERE tag = 'adcash';

UPDATE client
SET negotiation_status_fields = '{
  "TAGS_BEING_ADDED": {
    "label": "Tags being added"
  },
  "AGREEMENT_REACHED": {
    "label": "Agreement reached"
  },
  "OTHER": {
    "label": "Other"
  }
}'
WHERE tag = 'adcashPoc';

-- Assign POC negotiations to the correct client
UPDATE negotiation
SET client_id = (SELECT id FROM client WHERE tag = 'adcashPoc')
WHERE create_time < '2020-08-01 00:00:00.000000 +00:00'
  AND client_id = (SELECT id FROM client WHERE tag = 'adcash');


-- Migrate previous statuses to correct ones
UPDATE negotiation
set status = 'OTHER'
WHERE client_id = (SELECT id FROM client WHERE tag = 'adcash')
  AND status IN ('ONGOING', 'GIVE_FEEDBACK', 'Ongoing', 'END');

UPDATE negotiation
set status = 'NEGOTIATION_CREATED'
WHERE client_id = (SELECT id FROM client WHERE tag = 'adcash')
  AND status IS NULL;

UPDATE negotiation
set status = 'AGREEMENT_REACHED'
WHERE client_id = (SELECT id FROM client WHERE tag = 'adcash')
  AND status = 'Agreement reached';

UPDATE negotiation
set status = 'NEGOTIATION_INTEREST_INDICATED'
WHERE client_id = (SELECT id FROM client WHERE tag = 'adcash')
  AND status = 'CHOOSE_INTEREST';

UPDATE negotiation
set status = 'NEGOTIATION_TRADING_STARTED'
WHERE client_id = (SELECT id FROM client WHERE tag = 'adcash')
  AND status = 'START_NEGOTIATING';

UPDATE negotiation
set status = 'NEGOTIATION_STARTED'
WHERE client_id = (SELECT id FROM client WHERE tag = 'adcash')
  AND status = 'BEGINNING';
