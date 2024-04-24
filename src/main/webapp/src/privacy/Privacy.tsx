import React from 'react';
import './privacy.scss';
import { Header } from '../header/Header';
import { GeneratedPrivacyContent } from './GeneratedPrivacyContent';

export const Privacy = () => {
  return (
    <div className="landing contentWrapper privacy">
      <Header />
      <section className="content-area">
        <div className="customContainer">
          <div className="row">
            <div className="col-lg-8">
              <GeneratedPrivacyContent />
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};
