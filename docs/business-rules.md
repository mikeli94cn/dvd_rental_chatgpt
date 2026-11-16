# Planned business rules

Inventory: AVAILABLE → RENTED on allocation. Staff return confirmation moves a
copy to AVAILABLE after passing inspection, or MAINTENANCE/DAMAGED. A lost copy
moves from RENTED to LOST through a staff resolution. Retired copies cannot rent.
Inspection does not make a copy available until staff confirm its result.

Item: ACTIVE → RETURNED or LOST. These are terminal; repeated returns fail with 409.
Order: ACTIVE while any item is active, CLOSED once every item is resolved.
Customer requests cannot directly complete returns or modify copy states.

Store daily rental and overdue rates on each rental item. Compute base charge as
rental days × daily rate, and overdue charge as max(0, days after due date) ×
overdue rate. Snapshot rates remain stable after catalog price changes. Define
currency explicitly and round charges to two decimal places. No real payments.

Allocate all copies of an order atomically, locking rows in ascending ID order.
Reject duplicate copy IDs and unavailable copies. Roll back the whole order on
failure. Customer endpoints must enforce ownership in backend queries.

These rental workflows are planned and are not implemented in the catalog milestone.
