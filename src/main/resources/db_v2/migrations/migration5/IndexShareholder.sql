CREATE UNIQUE INDEX shareholder_idx ON
    major_shareholder (
                       name_major_stakeholders,
                       shareholding_ratio
        )
    WHERE deleted IS false