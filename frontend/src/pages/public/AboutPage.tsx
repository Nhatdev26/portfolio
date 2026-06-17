import { useQuery } from "@tanstack/react-query";

import { getPublicProfile } from "../../services/profile";

export function AboutPage() {
  const profile = useQuery({
    queryKey: ["public-profile", "about", "EN"],
    queryFn: () => getPublicProfile("EN"),
    retry: false
  });

  return (
    <section className="profile-public about-showcase">
      <div className="about-copy">
        <p className="eyebrow">About</p>
        <h1>{profile.data?.displayName ?? "About"}</h1>
        <p className="hero-text">
          {profile.data?.longBio ??
            "This route is reserved for the public profile vertical slice."}
        </p>
      </div>
      {profile.data && (
        <dl className="profile-facts">
          <div>
            <dt>Role</dt>
            <dd>{profile.data.primaryRole}</dd>
          </div>
          <div>
            <dt>Focus</dt>
            <dd>{profile.data.mainTechFocus}</dd>
          </div>
          <div>
            <dt>Location</dt>
            <dd>{profile.data.location}</dd>
          </div>
        </dl>
      )}
    </section>
  );
}
