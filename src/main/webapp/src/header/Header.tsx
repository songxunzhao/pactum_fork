import React from 'react';
import './header.scss';
import logo from './img/logo.png';
export const Header = () => {
  return (
    <header className="header-area">
      <div className="customContainer">
        <div className="row">
          <div className="col-lg-12">
            <div className="logo-area">
              <a href="/">
                <img src={logo} alt="Pactum" />
              </a>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
};
