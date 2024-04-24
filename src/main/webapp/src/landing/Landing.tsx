import React from 'react';
import './landing.scss';
import { saveEmail, sendEmailToGA } from './landingAction';
import { connect, ResolveThunks } from 'react-redux';
import { AppState } from '../store/store';
import { RouteComponentProps } from 'react-router';
import { Header } from '../header/Header';
import ChatWrapper from '../Chat/ChatWrapper';

export const Landing = ({ saveEmailAction, email, state, error }: ConnectProps) => {
  function onValueChanged(event: React.ChangeEvent<HTMLInputElement>) {
    email = event.target.value;
  }

  function onFormSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    saveEmailAction(email);
    sendEmailToGA('email');
  }

  function getSubscriptionResult() {
    if (state === 'subscribed') {
      return <p className="text-success">Thanks we{"'"}ll be in touch.</p>;
    } else if (error === 'Member Exists') {
      return <p className="text-danger">This email address has already been sent to us.</p>;
    } else if (error) {
      return <p className="text-danger">There was an error while subscribing. Please try again!</p>;
    }
  }

  return (
    <div className="landing contentWrapper">
      <Header />

      <section className="content-area">
        <div className="customContainer">
          <div className="row">
            <div className="col-lg-6">
              <div className="rsc-container">
                <ChatWrapper showPoweredBy={false} />
              </div>
            </div>
            <div className="col-lg-6">
              <div className="content-right">
                <h3>
                  Pactum helps companies to unlock value from long tail by automatically negotiating
                  contracts on a massive scale
                </h3>
                <p>
                  The system learns to understand the value of different contract terms and uses AI
                  to perform high-quality and considerate negotiations that reach a win-win outcome.
                </p>
                <p>
                  We believe that in the near future, an organization’s strategic work will be done
                  by humans while most of the operations and execution will be left to machines.
                </p>
                <h3 className="interestedStyle">Read More</h3>
                <p>
                  <ul>
                    <li>
                      <a href="/Pactum_press_release.pdf" target="_blank">
                        Press release
                      </a>{' '}
                      announcing the launch
                      <br />(
                      <a
                        href="https://fortune.com/2019/09/11/skype-mafia-backs-ai-startup-automating-contract-negotiations/"
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        Fortune
                      </a>
                      ), Sept 2019.
                    </li>
                    <li>
                      <a href="/Pactum_press_release_2.pdf" target="_blank">
                        Press release
                      </a>{' '}
                      announcing collaboration with Walmart (
                      <a
                        href="https://www.nasdaq.com/articles/heres-how-walmart-is-using-artificial-intelligence-to-keep-prices-low-2020-03-27"
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        Nasdaq
                      </a>
                      ), March 2020.
                    </li>
                    <li>
                      <a href="/Pactum_press_release_3.pdf" target="_blank">
                        Press release
                      </a>{' '}
                      announcing the seed round (
                      <a
                        href="https://tech.eu/brief/pactum-seed/"
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        Tech.eu
                      </a>
                      ), June 2020.
                    </li>
                  </ul>
                </p>
                <h3 className="interestedStyle">Interested?</h3>
                <p>
                  Request a demo from <a href="mailto:info@pactum.com">info@pactum.com</a>.
                </p>
                <div className="subscribe-part">
                  <p>Leave your e-mail and we{"'"}ll contact you shortly!</p>
                  <form onSubmit={event => onFormSubmit(event)}>
                    <input
                      onChange={event => onValueChanged(event)}
                      type="email"
                      name="email"
                      placeholder="Your e-mail"
                      required
                    />
                    <input type="submit" value="Send" />
                  </form>
                  {getSubscriptionResult()}
                </div>
                <h3>&nbsp;</h3>
                <p style={{ textAlign: 'center' }}>
                  <a href="https://jobs.lever.co/pactum" rel="noopener noreferrer" target="_blank">
                    Careers
                  </a>
                  {' • '}
                  <a href="https://medium.com/pactum-ai" rel="noopener noreferrer" target="_blank">
                    Blog
                  </a>
                  {' • '}
                  <a href="/privacy" rel="noopener noreferrer">
                    Privacy Policy
                  </a>
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

const mapStateToProps = ({ landing }: AppState) => ({
  email: landing.email,
  state: landing.state,
  error: landing.error,
});

const mapDispatchToProps = {
  saveEmailAction: saveEmail,
};

interface MatchParams {
  flowId: string;
  modelId: string;
  modelKey: string;
  stateId: string;
  delay?: string;
}

type ConnectProps = ReturnType<typeof mapStateToProps> &
  ResolveThunks<typeof mapDispatchToProps> &
  RouteComponentProps<MatchParams>;

export default connect(
  mapStateToProps,
  mapDispatchToProps,
)(Landing);
